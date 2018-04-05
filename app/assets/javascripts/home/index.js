/**
 * The Home module.
 *
 * Shows the start page and provides controllers for the header and the footer.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import CommonModule    from '../common'
import UsersModule     from '../users'
import angular         from 'angular'
import angularUiRouter from '@uirouter/angularjs'

/**
 * AngularJS components related to the home page.
 * @namespace home
 */

/**
 * AngularJS components related to the home page.
 * @namespace home.components
 */

/**
 * A Webpack module for the Biobank AngularJS *home* layer.
 *
 * @memberOf home
 */
const ngHomeModule = angular.module('biobank.home', [
  CommonModule,
  UsersModule,
  angularUiRouter
])

const context = require.context('./', true, /^(?:.(?![\\\/]modules[\\\/]|index\.js|Spec\.js))*\.js$/)

context.keys().forEach(key => {
  context(key).default(ngHomeModule)
})

export default ngHomeModule.name
