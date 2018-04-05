/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import angular from 'angular';
import angularGettext  from 'angular-gettext'

/**
 * AngularJS components available to the rest of the application.
 * @namespace base
 */

/**
 * A Webpack module for the Biobank AngularJS base layer.
 *
 * @memberOf base
 */
const ngBaseLayerModule = angular.module('biobank.base', [ angularGettext ])

/**
 * AngularJS services available to the rest of the application.
 * @namespace base.services
 */

const context = require.context('./', true, /^(?:.(?![\\\/]modules[\\\/]|index\.js|Spec\.js))*\.js$/)

context.keys().forEach(key => {
  context(key).default(ngBaseLayerModule)
})


export default ngBaseLayerModule.name
