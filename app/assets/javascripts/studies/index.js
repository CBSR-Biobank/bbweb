/**
 * Studies configuration module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import DomainModule    from '../domain'
import angular from 'angular';
import angularGettext  from 'angular-gettext'

/**
 * AngularJS components related to {@link domain.studies.Study Studies}.
 * @namespace studies
 */

/**
 * AngularJS services related to {@link domain.studies.Study Studies}.
 * @namespace studies.services
 */

/**
 * A Webpack module for the Biobank AngularJS *studies* layer.
 *
 * @memberOf studies
 */
const ngStudiesModule = angular.module('biobank.studies', [
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
    dep.default(ngStudiesModule)
  })

export default ngStudiesModule.name
