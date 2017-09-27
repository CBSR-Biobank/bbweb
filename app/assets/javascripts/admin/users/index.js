/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import StudiesModule from '../../studies';
import angular       from 'angular';

const AdminUsersModule = angular.module('biobank.admin.users', [ StudiesModule ])
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
                 require('./components/membershipsPagedList/membershipsPagedListComponent'))
      .name;

export default AdminUsersModule;
