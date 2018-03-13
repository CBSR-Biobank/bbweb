/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * AngularJS components related to {@link domain.participants.Specimen Specimen} collection.
 * @namespace collection
 */

/**
 * AngularJS Components related to {@link domain.participants.Specimen Specimen} collection.
 * @namespace collection.components
 */

/**
 * AngularJS services related to {@link domain.participants.Specimen Specimen} collection.
 * @namespace collection.services
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

/**
 * A Webpack module for user specimen collection.
 *
 * @memberOf collection
 * @type {AngularJS_Module}
 */
const ngCollectionModule = angular.module('biobank.collection', [
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
  context(key).default(ngCollectionModule)
})

export default ngCollectionModule.name
