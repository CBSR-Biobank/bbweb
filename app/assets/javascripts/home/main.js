/**
 * The Home module.
 *
 * Shows the start page and provides controllers for the header and the footer.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
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
