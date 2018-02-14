/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import angular from 'angular';
import angularGettext  from 'angular-gettext'

const ngModule = angular.module('biobank.base', [ angularGettext ])

const context = require.context('./', true, /^(?:.(?![\\\/]modules[\\\/]|index\.js|Spec\.js))*\.js$/)

context.keys().forEach(key => {
  context(key).default(ngModule)
})

export default ngModule.name
