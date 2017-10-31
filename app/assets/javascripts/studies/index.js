/**
 * Studies configuration module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import DomainModule    from '../domain'
import angular from 'angular';
import angularGettext  from 'angular-gettext'

const ngModule = angular.module('biobank.studies', [
  DomainModule,
  angularGettext
])

const contextList = [
  require.context('./services',   true, /^(.(?!Spec))*\.js$/)
]

contextList
  .reduce((deps, context) => (
    deps.concat(context.keys().map(context))
  ), [])
  .forEach(dep => {
    dep.default(ngModule)
  })

export default ngModule.name
