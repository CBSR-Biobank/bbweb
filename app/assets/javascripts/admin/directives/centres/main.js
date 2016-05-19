/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
*/
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.admin.directives.centres',
      module;

  module = angular.module(name, ['biobank.users']);

  module.directive('centreAdd',          require('./centreAdd/centreAddDirective'));
  module.directive('centreLocationAdd',  require('./centreLocationAdd/centreLocationAddDirective'));
  module.directive('centreLocationView',  require('./centreLocationView/centreLocationViewDirective'));
  module.directive('centreSummary',      require('./centreSummary/centreSummaryDirective'));
  module.directive('centreView',         require('./centreView/centreViewDirective'));
  module.directive('centresList',        require('./centresList/centresListDirective'));
  module.directive('centreStudiesPanel', require('./centreStudiesPanel/centreStudiesPanelDirective'));
  module.directive('locationsPanel',     require('./locationsPanel/locationsPanelDirective'));

  return {
    name: name,
    module: module
  };
});
