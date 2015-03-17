/**
 * Main, shows the start page and provides controllers for the header and the footer.
 * This the entry module which serves as an entry point so other modules only have to include a
 * single module.
 */
define([
  'angular',
  './FooterCtrl',
  './HeaderCtrl',
  './HomeCtrl',
  './states',
], function(angular,
            FooterCtrl,
            HeaderCtrl,
            HomeCtrl,
            states) {
  'use strict';

  var module = angular.module('biobank.home', []);

  module.controller('FooterCtrl', FooterCtrl);
  module.controller('HeaderCtrl', HeaderCtrl);
  module.controller('HomeCtrl', HomeCtrl);
  module.config(states);

  return module;
});
