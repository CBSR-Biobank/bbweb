/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import CommonModule    from '../../../common'
import StudiesModule   from '../../../studies'
import UsersModule     from '../../../users'
import angular         from 'angular'
import angularGettext  from 'angular-gettext'
import angularUiRouter from '@uirouter/angularjs'

/**
 * AngularJS components for {@link domain.users.User User} administration.
 * @namespace admin.users
 */

/**
 * A Webpack module for {@link domain.users.User User} Administration.
 *
 * @memberOf admin.users
 * @type {AngularJS_Module}
 */
const ngAdminUsersModule = angular.module('biobank.admin.users', [
  angularGettext,
  angularUiRouter,
  CommonModule,
  StudiesModule,
  UsersModule
])
const context = require.context('./', true, /^(?:.(?![\\\/]modules[\\\/]|index\.js|Spec\.js))*\.js$/)

context.keys().forEach(key => {
  context(key).default(ngAdminUsersModule)
})

/**
 * AngularJS components for {@link domain.users.User User} administration.
 * @namespace admin.users.components
 */

/**
 * AngularJS services for {@link domain.users.User User} administration.
 * @namespace admin.users.services
 */

export default ngAdminUsersModule.name
