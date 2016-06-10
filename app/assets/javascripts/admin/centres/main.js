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

  module.directive('centreAdd',          require('./directives/centreAdd/centreAddDirective'));
  module.directive('centreLocationAdd',  require('./directives/centreLocationAdd/centreLocationAddDirective'));
  module.directive('centreLocationView', require('./directives/centreLocationView/centreLocationViewDirective'));
  module.directive('centreSummary',      require('./directives/centreSummary/centreSummaryDirective'));
  module.directive('centreView',         require('./directives/centreView/centreViewDirective'));
  module.directive('centresList',        require('./directives/centresList/centresListDirective'));
  module.directive('centreStudiesPanel', require('./directives/centreStudiesPanel/centreStudiesPanelDirective'));
  module.directive('locationsPanel',     require('./directives/locationsPanel/locationsPanelDirective'));

  module.config(require('./states'));

  return {
    name: name,
    module: module
  };
});
