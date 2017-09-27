/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import angular from 'angular';
import biobankUsers from '../../users';

const MODULE_NAME = 'biobank.admin.centres';

angular.module(MODULE_NAME, [ biobankUsers ])
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

export default MODULE_NAME;
