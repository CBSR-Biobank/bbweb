/**
 * AngularJS components related to {@link domain.centres.Centre Centres}.
 *
 * @namespace centres
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * AngularJS components related to {@link domain.centres.Centre Centres}.
 * @namespace centres.components
 */

/**
 * AngularJS controller base classes related to {@link domain.centres.Centre Centres}.
 * @namespace centres.controllers
 */

/**
 * AngularJS services related to {@link domain.centres.Centre Centres}.
 * @namespace centres.services
 */

/**
 * AngularJS services related to {@link domain.centres.Centre Centres}.
 * @namespace centres.services
 */

import CommonModule    from '../common'
import DomainModule    from '../domain'
import HomeModule      from '../home'
import angular         from 'angular'
import angularGettext  from 'angular-gettext'
import angularSanitize from 'angular-sanitize'
import angularToastr   from 'angular-toastr'
import angularUiRouter from '@uirouter/angularjs'
import uiBootstrap     from 'angular-ui-bootstrap'

/**
 * A Webpack module for user interactions with {@link domain.centres.Centre Centres}.
 *
 * @memberOf centres
 * @type {AngularJS_Module}
 */
const ngCentresModule = angular.module(
  'biobank.centres',
  [
    angularGettext,
    angularSanitize,
    angularToastr,
    angularUiRouter,
    uiBootstrap,
    CommonModule,
    DomainModule,
    HomeModule
  ])

const context = require.context('./', true, /^(?:.(?![\\\/]modules[\\\/]|index\.js|Spec\.js))*\.js$/)

context.keys().forEach(key => {
  context(key).default(ngCentresModule)
})

/**
 * Used by componet {@link centres.shipmentsTable shipmentsTable} to specify the type of specimens it is
 * displaying.
 *
 * @enum {string}
 * @memberOf centres
 */
const SHIPMENT_TYPES = {
  INCOMING:  'incoming',
  OUTGOING:  'outgoing',
  COMPLETED: 'completed'
};

ngCentresModule.constant('SHIPMENT_TYPES', SHIPMENT_TYPES);

export default ngCentresModule.name
