/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
import CentresModule from '../centres';
import CommonModule  from '../common';
import angular       from 'angular';

/**
 * AngularJS components used for administration.
 * @namespace admin
 */

const loadModules = require.context('./modules', true, /[\\\/]index\.js$/)

const moduleNames = []
loadModules.keys().forEach((key) => {
  moduleNames.push(loadModules(key).default)
})

/**
 * A Webpack module for Biobank Administration.
 *
 * @memberOf admin
 * @type {AngularJS_Module}
 */
const ngAdminModule = angular.module('biobank.admin', [ CommonModule, CentresModule ].concat(moduleNames))

const context = require.context('./', true, /^(?:.(?![\\\/]modules[\\\/]|index\.js|Spec\.js))*\.js$/)

context.keys().forEach(key => {
  context(key).default(ngAdminModule)
})

export default ngAdminModule.name
