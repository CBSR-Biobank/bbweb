#!/usr/bin/env node
'use strict';

/**
 * Displays a table of all the JavaScript files in the 'app' directory and whether or not there is a
 * Jasmine test suite for that file in the 'test' directory.
 */
require('console.table');

var program = require('commander'),
    path    = require('path'),
    fs      = require('fs'),
    _       = require('lodash'),
    sprintf = require('sprintf-js').sprintf;

program.parse(process.argv);

var appfiles = dirGetFiles('app/assets/javascripts'),
    testfiles = dirGetFiles('test/assets/javascripts');

var jsfiles = getJsFiles(appfiles),
    suitefiles = getJsSuiteFiles(testfiles);

checkForAllSuites(jsfiles, suitefiles);

/**
 * Walks the directory tree looking for files.
 */
function dirGetFiles(dir) {
  var result;

  if (!fs.existsSync(dir)) {
    return [];
  }

  result = fs.readdirSync(dir)
    .filter(function (f) {
      return f && (f[0] !== '.'); // Ignore hidden files
    })
    .map(function (f) {
      var p = path.join(dir, f),
	  stat = fs.statSync(p);

      if (stat.isDirectory()) {
	return dirGetFiles(p);
      }

      return path.join(p);
    });

  return _.flatMap(result);
}

function getJsFiles(files) {
  return _.filter(files, function (item) {
    return (item.match(/\.js$/) && !item.match(/(app|main|states).js/));
  });
}

function getJsSuiteFiles(files) {
  return _.filter(getJsFiles(files), function (item) {
    return (!item.match(/test\/assets\/javascripts\/test\/assets\/javascripts\/test/) &&
            !item.match(/^test-main\.js$/));
  });
}

function jsPathToSuitePath(jspath) {
  return jspath.replace(/^app/, 'test').replace(/\.js$/, 'Spec.js');
}

function hasSuite(jspath, suitefiles) {
  var suiteToFind = jsPathToSuitePath(jspath);
  //console.log('to find:', suiteToFind);
  return suitefiles.indexOf(suiteToFind) > 0;
}

function checkForAllSuites(jsfiles, suitefiles) {
  var table = _.map(jsfiles, function (jsfile) {
    return { suite: hasSuite(jsfile, suitefiles) ? 'Yes' : '', file: jsfile };
  });
  console.table(table);
}
