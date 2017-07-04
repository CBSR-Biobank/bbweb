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

  module.component('centresPagedList',   require('./components/centresPagedList/centresPagedListComponent'));
  module.component('centreAdd',          require('./components/centreAdd/centreAddComponent'));
  module.component('centreLocationAdd',  require('./components/centreLocationAdd/centreLocationAddComponent'));

  module.directive('centreLocationView', require('./directives/centreLocationView/centreLocationViewDirective'));
  module.directive('centreSummary',      require('./directives/centreSummary/centreSummaryDirective'));
  module.directive('centreView',         require('./directives/centreView/centreViewDirective'));
  module.directive('centresAdmin',       require('./directives/centresAdmin/centresAdminDirective'));
  module.directive('centreStudiesPanel', require('./directives/centreStudiesPanel/centreStudiesPanelDirective'));
  module.directive('locationsPanel',     require('./directives/locationsPanel/locationsPanelDirective'));

  module.config(require('./states'));

  return {
    name: name,
    module: module
  };
});
