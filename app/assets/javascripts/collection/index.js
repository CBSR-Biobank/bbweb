/**
 * The Specimen collection module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import DomainModule    from '../domain'
import StudiesModule   from '../studies'
import UsersModule     from '../users'
import angular         from 'angular'
import angularGettext  from 'angular-gettext'
import angularSanitize from 'angular-sanitize';
import angularToastr   from 'angular-toastr';
import angularUiRouter from '@uirouter/angularjs'
import uiBootstrap     from 'angular-ui-bootstrap'

const ngModule = angular.module('biobank.collection', [
  angularUiRouter,
  angularGettext,
  angularSanitize,
  angularToastr,
  uiBootstrap,
  DomainModule,
  StudiesModule,
  UsersModule
])

const context = require.context('./', true, /^(?:.(?![\\\/]modules[\\\/]|index\.js|Spec\.js))*\.js$/)

context.keys().forEach(key => {
  context(key).default(ngModule)
})

export default ngModule.name
