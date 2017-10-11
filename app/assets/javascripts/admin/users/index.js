/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import StudiesModule        from '../../studies';
import angular              from 'angular';
import membershipsPagedList from './components/membershipsPagedList/membershipsPagedListComponent';
import membershipView       from './components/membershipView/membershipViewComponent';
import usersPagedList       from './components/usersPagedList/usersPagedListComponent';

const AdminUsersModule = angular.module('biobank.admin.users', [ StudiesModule ])
      .config(require('./states'))
      .component('manageUsers',     require('./components/manageUsers/manageUsersComponent'))
      .component('userAdmin',       require('./components/userAdmin/userAdminComponent'))
      .component('usersPagedList',  usersPagedList)
      .component('userProfile',     require('./components/userProfile/userProfileComponent'))
      .component('userRoles',       require('./components/userRoles/userRolesComponent'))
      .component('membershipAdmin', require('./components/membershipAdmin/membershipAdminComponent'))
      .component('membershipAdd',   require('./components/membershipAdd/membershipAddComponent'))
      .component('membershipView',  membershipView)
      .component('membershipsPagedList', membershipsPagedList)
      .name;

export default AdminUsersModule;
