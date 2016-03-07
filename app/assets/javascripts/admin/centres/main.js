/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
*/
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.admin.centres',
      module;

  module = angular.module(name, ['biobank.users']);

  module.controller('CentreCtrl',           require('./CentreCtrl'));
  module.controller('CentreEditCtrl',       require('./CentreEditCtrl'));
  module.controller('CentreSummaryTabCtrl', require('./CentreSummaryTabCtrl'));
  module.controller('CentresCtrl',          require('./CentresCtrl'));
  module.controller('LocationEditCtrl',     require('./LocationEditCtrl'));

  module.config(require('./states'));

  return {
    name: name,
    module: module
  };
});
