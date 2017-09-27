/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import angular       rom 'angular';
import biobankStudies from '../studies';

const MODULE_NAME = 'biobank.admin.users';

angular
  .module(MODULE_NAME, [ biobankStudies ])
  .config(require('./states'))
  .component('manageUsers',     require('./components/manageUsers/manageUsersComponent'))
  .component('userAdmin',       require('./components/userAdmin/userAdminComponent'))
  .component('usersPagedList',  require('./components/usersPagedList/usersPagedListComponent'))
  .component('userProfile',     require('./components/userProfile/userProfileComponent'))
  .component('userRoles',       require('./components/userRoles/userRolesComponent'))
  .component('membershipAdmin', require('./components/membershipAdmin/membershipAdminComponent'))
  .component('membershipAdd',   require('./components/membershipAdd/membershipAddComponent'))
  .component('membershipView',  require('./components/membershipView/membershipViewComponent'))
  .component('membershipsPagedList',
             require('./components/membershipsPagedList/membershipsPagedListComponent'));

export default MODULE_NAME;
