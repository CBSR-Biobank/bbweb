/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import DomainModule    from '../domain'
import angular         from 'angular';
import angularGettext  from 'angular-gettext'
import angularSanitize from 'angular-sanitize'
import angularToastr   from 'angular-toastr'
import angularUiRouter from '@uirouter/angularjs'

/**
 * AngularJS components available to the rest of the application.
 * @namespace common
 */

/**
 * AngularJS Controllers available to the rest of the application.
 * @namespace common.controllers
 */

/**
 * AngularJS Directives available to the rest of the application.
 * @namespace common.directives
 */

/**
 * AngularJS Filters available to the rest of the application.
 * @namespace common.filters
 */

const loadModules = require.context('./modules', true, /[\\\/]index\.js$/)

const moduleNames = []
loadModules.keys().forEach((key) => {
  moduleNames.push(loadModules(key).default)
})

const ngModule = angular.module('biobank.common', [
  angularGettext,
  angularSanitize,
  angularToastr,
  angularUiRouter,
  DomainModule
].concat(moduleNames))

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
