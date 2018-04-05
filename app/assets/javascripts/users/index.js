/**
 * User configuration module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import CommonModule   from '../common'
import angular        from 'angular'
import angularCookies from 'angular-cookies'

/**
 * AngularJS components related to {@link domain.users.Users}.
 * @namespace users
 */

/**
 * AngularJS services related to {@link domain.users.Users}.
 * @namespace users.services
 */

/**
 * A Webpack module for the Biobank AngularJS *users* layer.
 *
 * @memberOf users
 */
const ngUsersModule = angular.module('biobank.users',
                                     [
                                       angularCookies,
                                       CommonModule
                                     ]);

const context = require.context('./', true, /^(.(?!index|Spec))*\.js$/)

context.keys().forEach(key => {
  context(key).default(ngUsersModule)
})

export default ngUsersModule.name
