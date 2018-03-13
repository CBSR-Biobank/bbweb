/**
 * Test module
 *
 * Provides factories for various client side domain objects.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import angular from 'angular';

const ngModule = angular.module('biobank.test', [])

const contextList = [
  require.context('./mixins', true, /\.js$/),
  require.context('./services', true, /\.js$/)
]

contextList
  .reduce((deps, context) => (
    deps.concat(context.keys().map(context))
  ), [])
  .forEach(dep => {
    dep.default(ngModule)
  })

export default ngModule.name
