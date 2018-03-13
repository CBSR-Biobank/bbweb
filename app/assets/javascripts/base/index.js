/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import angular from 'angular';
import angularGettext  from 'angular-gettext'

/**
 * A Webpack module for the Biobank AngularJS base layer.
 *
 * @memberOf base
 * @type {AngularJS_Module}
 */
const ngBaseLayerModule = angular.module('biobank.base', [ angularGettext ])

const context = require.context('./', true, /^(?:.(?![\\\/]modules[\\\/]|index\.js|Spec\.js))*\.js$/)

context.keys().forEach(key => {
  context(key).default(ngBaseLayerModule)
})


export default ngBaseLayerModule.name
