/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.admin.studies.processing',
      module;

  module = angular.module(name, [ 'biobank.users' ]);

  module.config(require('./states'));

  module.controller('ProcessingTypeEditCtrl', require('./ProcessingTypeEditCtrl'));
  module.controller('SpcLinkTypeEditCtrl',    require('./SpcLinkTypeEditCtrl'));

  return {
    name: name,
    module: module
  };
});
