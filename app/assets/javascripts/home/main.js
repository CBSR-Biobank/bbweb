/**
 * The Home module.
 *
 * Shows the start page and provides controllers for the header and the footer.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.home',
      module;

  module = angular.module(name, []);

  module.config(require('./states'));

  module.controller('FooterCtrl', require('./FooterCtrl'));
  module.controller('HeaderCtrl', require('./HeaderCtrl'));
  module.controller('HomeCtrl',   require('./HomeCtrl'));

  return {
    name: name,
    module: module
  };
});
