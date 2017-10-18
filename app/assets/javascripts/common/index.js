/**
 * Angular module for common functionality.
 * @namespace common
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import angular from 'angular';

const loadModules = require.context('./modules', true, /[\\\/]index\.js$/)

const moduleNames = []
loadModules.keys().forEach((key) => {
  moduleNames.push(loadModules(key).default)
})

const ngModule = angular.module('biobank.common', moduleNames)

ngModule.run(loadTemplates)

/* @ngInject */
function loadTemplates($templateCache) {
  $templateCache.put('smartTablePaginationTemplate.html', require('./smartTablePaginationTemplate.html'));
}

const context = require.context('./', true, /^(?:.(?![\\\/]modules[\\\/]|index\.js|Spec\.js))*\.js$/)

//console.log(context.keys())

context.keys().forEach(key => {
  context(key).default(ngModule)
})

export default ngModule.name
