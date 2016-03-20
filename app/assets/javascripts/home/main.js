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

  module.directive('biobankFooter', require('./directives/biobankFooter/biobankFooterDirective'));
  module.directive('biobankHeader', require('./directives/biobankHeader/biobankHeaderDirective'));
  module.directive('home',            require('./directives/home/homeDirective'));

  return {
    name: name,
    module: module
  };
});
