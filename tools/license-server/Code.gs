/**
 * MonetizeKit license server (Google Apps Script web app + Google Sheet).
 *
 * Setup
 * 1. Create a Google Sheet, open Extensions > Apps Script and paste this file.
 * 2. Run `setup` once from the editor (creates the "Apps" tab and asks for permission).
 * 3. Deploy > New deployment > type "Web app", Execute as "Me", Who has access "Anyone".
 *    Copy the URL that ends with /exec.
 * 4. Put that URL in LICENSE_ENDPOINT in
 *    MonetizeKit/src/main/java/io/monetize/kit/sdk/core/utils/license/AdKitLicenseManager.kt
 *    and release the library. Re-deploying a new version: Deploy > Manage deployments > Edit,
 *    so the URL stays the same.
 *
 * How it works
 * Every app using the library calls doGet (at most every 12 hours per device) with its package,
 * app name, version, library version and signing-certificate SHA-256. Each package + certificate
 * pair is one row. Set Status to DENY to switch ads off and crash that app (on its next check,
 * up to 12 hours), ALLOW to let it run again (a denied app re-checks on every start).
 */

const SHEET_NAME = 'Apps';

// Status for a package + certificate pair seen for the first time.
const NEW_APP_STATUS = 'ALLOW';

// When true, a known package showing up with a new certificate (for example a re-signed copy)
// starts as DENY. Off by default because your own debug / upload-key builds also have other
// certificates than the Play-signed release.
const DENY_NEW_CERT_FOR_KNOWN_PACKAGE = false;

// Rows are read from cache and "Last seen" is written at most this often, to keep load low.
const CACHE_SECONDS = 300;
const LAST_SEEN_UPDATE_MS = 60 * 60 * 1000;

const HEADERS = ['Package', 'App name', 'Signing SHA-256', 'Status', 'Version name',
  'Version code', 'Library version', 'First seen', 'Last seen'];
const COL = {
  PACKAGE: 0, NAME: 1, SHA: 2, STATUS: 3, VERSION_NAME: 4,
  VERSION_CODE: 5, LIBRARY: 6, FIRST: 7, LAST: 8,
};
const CACHE_KEY = 'apps_rows';

function setup() {
  getSheet_();
}

// Runs automatically when you edit the sheet by hand (for example ALLOW -> DENY), so the
// next request reads the new value instead of the cached rows
function onEdit(e) {
  CacheService.getScriptCache().remove(CACHE_KEY);
}

function doGet(e) {
  const params = (e && e.parameter) || {};
  const report = {
    pkg: clean_(params.package),
    name: clean_(params.appName),
    sha: clean_(params.sha256),
    versionName: clean_(params.versionName),
    versionCode: clean_(params.versionCode),
    library: clean_(params.libraryVersion),
  };
  // Never break an app because of a malformed request or a server problem
  if (!report.pkg) return json_({ allowed: true });

  try {
    const cached = findRow_(readRows_(), report);
    if (cached.index !== -1 && !needsWrite_(cached.row, report)) {
      return json_({ allowed: isAllowed_(cached.row[COL.STATUS]) });
    }
    return json_({ allowed: isAllowed_(writeReport_(report)) });
  } catch (error) {
    // Logged under Executions in the Apps Script editor; the app is still allowed
    console.error('doGet failed for ' + report.pkg + ': ' + error);
    return json_({ allowed: true });
  }
}

// Run from the editor to see what doGet answers for a package (result in the execution log)
function testLicense() {
  const result = doGet({ parameter: { package: 'test.app.test2', appName: 'Test1' } });
  console.log(result.getContent());
}

function writeReport_(report) {
  const lock = LockService.getScriptLock();
  lock.waitLock(10000);
  try {
    const sheet = getSheet_();
    const rows = sheet.getDataRange().getValues();
    const found = findRow_(rows, report);
    const now = new Date();
    let status;

    if (found.index === -1) {
      status = DENY_NEW_CERT_FOR_KNOWN_PACKAGE && found.packageKnown ? 'DENY' : NEW_APP_STATUS;
      const newRow = sheet.getLastRow() + 1;
      // Plain text, so the sheet doesn't turn versions like "1.0" into numbers
      sheet.getRange(newRow, 1, 1, COL.LIBRARY + 1).setNumberFormat('@');
      sheet.getRange(newRow, 1, 1, HEADERS.length).setValues([[report.pkg, report.name,
        report.sha, status, report.versionName, report.versionCode, report.library, now, now]]);
    } else {
      status = String(found.row[COL.STATUS] || NEW_APP_STATUS);
      const sheetRow = found.index + 1;
      sheet.getRange(sheetRow, COL.NAME + 1).setValue(report.name);
      sheet.getRange(sheetRow, COL.VERSION_NAME + 1, 1, 3)
        .setValues([[report.versionName, report.versionCode, report.library]]);
      sheet.getRange(sheetRow, COL.LAST + 1).setValue(now);
    }

    CacheService.getScriptCache().remove(CACHE_KEY);
    return status;
  } finally {
    lock.releaseLock();
  }
}

// Write when "Last seen" is old or the app reports a different version than the sheet shows
function needsWrite_(row, report) {
  const lastSeen = new Date(row[COL.LAST]).getTime() || 0;
  return Date.now() - lastSeen > LAST_SEEN_UPDATE_MS ||
    String(row[COL.VERSION_NAME]) !== report.versionName ||
    String(row[COL.LIBRARY]) !== report.library;
}

function findRow_(rows, report) {
  let index = -1;
  let packageKnown = false;
  for (let i = 1; i < rows.length; i++) {
    if (String(rows[i][COL.PACKAGE]) !== report.pkg) continue;
    packageKnown = true;
    if (String(rows[i][COL.SHA]) === report.sha) {
      index = i;
      break;
    }
  }
  return { index: index, row: index === -1 ? null : rows[index], packageKnown: packageKnown };
}

function readRows_() {
  const cache = CacheService.getScriptCache();
  const cached = cache.get(CACHE_KEY);
  if (cached) return JSON.parse(cached);

  const rows = getSheet_().getDataRange().getValues();
  try {
    cache.put(CACHE_KEY, JSON.stringify(rows), CACHE_SECONDS);
  } catch (error) {
    // Too big for the cache (over 100 KB): just read the sheet each time
  }
  return rows;
}

function isAllowed_(status) {
  return String(status).trim().toUpperCase() !== 'DENY';
}

function getSheet_() {
  const spreadsheet = SpreadsheetApp.getActiveSpreadsheet();
  let sheet = spreadsheet.getSheetByName(SHEET_NAME);
  if (!sheet) {
    sheet = spreadsheet.insertSheet(SHEET_NAME);
    sheet.appendRow(HEADERS);
    sheet.setFrozenRows(1);
  }
  return sheet;
}

// Limits length and stops values like "=..." from being treated as sheet formulas
function clean_(value) {
  const text = String(value == null ? '' : value).trim().substring(0, 200);
  return /^[=+\-@]/.test(text) ? "'" + text : text;
}

function json_(body) {
  return ContentService.createTextOutput(JSON.stringify(body))
    .setMimeType(ContentService.MimeType.JSON);
}
