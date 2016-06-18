#!/usr/bin/env node
'use strict';

/* jshint esversion:6 */

/**
 * Displays a table of all the JavaScript files in the 'app' directory and whether or not there is a
 * Jasmine test suite for that file in the 'test' directory.
 */
require('console.table');

const program    = require('commander'),
      path       = require('path'),
      fs         = require('fs'),
      _          = require('lodash'),
      appfiles   = dirGetFiles('app/assets/javascripts'),
      testfiles  = dirGetFiles('test/assets/javascripts'),
      jsfiles    = getJsFiles(appfiles),
      suitefiles = getJsSuiteFiles(testfiles);

program.parse(process.argv);
checkForAllSuites(jsfiles, suitefiles);

/**
 * Walks the directory tree looking for files.
 */
function dirGetFiles(dir) {
  let result;

  if (!fs.existsSync(dir)) {
    return [];
  }

  result = fs.readdirSync(dir)
    .filter(f => f && (f[0] !== '.')) // Ignore hidden files
    .map(f => {
      let p = path.join(dir, f),
	  stat = fs.statSync(p);

      if (stat.isDirectory()) {
	return dirGetFiles(p);
      }

      return p;
    });

  return _.flatMap(result);
}

function getJsFiles(files) {
  return _.filter(files, item => item.match(/\.js$/) && !item.match(/(app|main|states).js/));
}

function getJsSuiteFiles(files) {
  return _.filter(getJsFiles(files), item =>
                  !item.match(/test\/assets\/javascripts\/test\/assets\/javascripts\/test/) &&
                  !item.match(/^test-main\.js$/));
}

function jsPathToSuitePath(jspath) {
  return jspath.replace(/^app/, 'test').replace(/\.js$/, 'Spec.js');
}

function hasSuite(jspath, suitefiles) {
  let suiteToFind = jsPathToSuitePath(jspath);
  //console.log('to find:', suiteToFind);
  return suitefiles.indexOf(suiteToFind) > 0;
}

function checkForAllSuites(jsfiles, suitefiles) {
  let table = _.map(jsfiles, jsfile =>
                    ({ suite: hasSuite(jsfile, suitefiles) ? 'Yes' : '', file: jsfile }));
  console.table(table);
}
