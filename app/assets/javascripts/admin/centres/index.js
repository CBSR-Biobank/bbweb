/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import UsersModule from '../../users';
import angular     from 'angular';

const AdminCentresModule = angular.module('biobank.admin.centres', [ UsersModule ])
      .component('centresPagedList',   require('./components/centresPagedList/centresPagedListComponent'))
      .component('centreAdd',          require('./components/centreAdd/centreAddComponent'))
      .component('centreLocationAdd',  require('./components/centreLocationAdd/centreLocationAddComponent'))
      .component('centreLocationView', require('./components/centreLocationView/centreLocationViewComponent'))
      .component('centreSummary',      require('./components/centreSummary/centreSummaryComponent'))
      .component('centreView',         require('./components/centreView/centreViewComponent'))
      .component('centresAdmin',       require('./components/centresAdmin/centresAdminComponent'))
      .component('centreStudiesPanel', require('./components/centreStudiesPanel/centreStudiesPanelComponent'))
      .component('locationsPanel',     require('./components/locationsPanel/locationsPanelComponent'))

      .config(require('./states'))
      .name;

export default AdminCentresModule;
