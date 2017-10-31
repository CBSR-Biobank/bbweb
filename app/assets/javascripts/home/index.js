/**
 * The Home module.
 *
 * Shows the start page and provides controllers for the header and the footer.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import CommonModule    from '../common'
import UsersModule     from '../users'
import angular         from 'angular'
import angularUiRouter from '@uirouter/angularjs'

const ngModule = angular.module('biobank.home', [
  CommonModule,
  UsersModule,
  angularUiRouter
])

const context = require.context('./', true, /^(?:.(?![\\\/]modules[\\\/]|index\.js|Spec\.js))*\.js$/)

context.keys().forEach(key => {
  context(key).default(ngModule)
})

export default ngModule.name
