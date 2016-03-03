/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.admin.studies',
      module,
      processing = require('./processing/main'),
      specimenGroups = require('./specimenGroups/main');

  module = angular.module(name, [
    processing.name,
    specimenGroups.name,
    'biobank.users'
  ]);

  module.config(require('./states'));
  module.config(require('./ceventTypes/states'));
  module.config(require('./participants/states'));

  module.controller('StudyCtrl',           require('./StudyCtrl'));
  module.controller('StudyEditCtrl',       require('./StudyEditCtrl'));
  module.controller('StudySummaryTabCtrl', require('./StudySummaryTabCtrl'));

  return {
    name: name,
    module: module
  };
});
