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

  module
    .component('centresPagedList',   require('./components/centresPagedList/centresPagedListComponent'))
    .component('centreAdd',          require('./components/centreAdd/centreAddComponent'))
    .component('centreLocationAdd',  require('./components/centreLocationAdd/centreLocationAddComponent'))
    .component('centreLocationView', require('./components/centreLocationView/centreLocationViewComponent'))
    .component('centreSummary',      require('./components/centreSummary/centreSummaryComponent'))
    .component('centreView',         require('./components/centreView/centreViewComponent'))
    .component('centresAdmin',       require('./components/centresAdmin/centresAdminComponent'))
    .component('centreStudiesPanel', require('./components/centreStudiesPanel/centreStudiesPanelComponent'))
    .component('locationsPanel',     require('./components/locationsPanel/locationsPanelComponent'))

    .config(require('./states'));

  return {
    name: name,
    module: module
  };
});
