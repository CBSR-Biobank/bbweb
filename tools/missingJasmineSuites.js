#!/usr/bin/env node

/* eslint-env es6 */
/* global process */
/* eslint no-sync: "off" */
/* eslint no-console: "off" */

const program    = require('commander'),
      path       = require('path'),
      fs         = require('fs'),
      _          = require('lodash');

require('console.table');

(function () {
  'use strict';

  /**
   * Displays a table of all the JavaScript files in the 'app' directory and whether or not there is a
   * Jasmine test suite for that file.
   */

  const allfiles = dirGetFiles('app/assets/javascripts');

  const appfiles = allfiles
        .filter((item) =>
                item.match(/\.js$/) &&
                !item.match(/\Spec.js$/) &&
                !item.match(/assets\/javascripts\/test/) &&
                !item.match(/(app|main|states|index).js/));

  const testsuites = allfiles
        .filter((item) =>
                item.match(/\Spec.js$/) &&
                !item.match(/assets\/javascripts\/test/) &&
                !item.match(/^test-main\.js$/));

  //console.log(testsuites);

  program.parse(process.argv);
  checkForAllSuites(appfiles, testsuites);

  /*
   * Walks the directory tree looking for files.
   */
  function dirGetFiles(dir) {
    if (!fs.existsSync(dir)) {
      return [];
    }

    const result = fs.readdirSync(dir)
          .filter((f) => f && (f[0] !== '.')) // Ignore hidden files
          .map((f) => {
            const p = path.join(dir, f);
            var stat = fs.statSync(p);

            if (stat.isDirectory()) {
              return dirGetFiles(p);
            }

            return p;
          });

    return _.flatMap(result);
  }

  function jsPathToSuitePath(jspath) {
    return jspath.replace(/\.js$/, 'Spec.js');
  }

  function hasSuite(jspath, suitefiles) {
    const testFileName = jsPathToSuitePath(jspath)
    //console.log(jspath, testFileName, suitefiles.indexOf(testFileName) >= 0);
    return suitefiles.indexOf(testFileName) >= 0;
  }

  function checkForAllSuites(jsfiles, suitefiles) {
    const table = _.map(jsfiles, (jsfile) =>
                      ({ suite: hasSuite(jsfile, suitefiles) ? 'Yes' : '', file: jsfile }));
    console.table(table);
  }
})();
