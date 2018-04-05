/**
 * Test module
 *
 * Provides factories for various client side domain objects.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import angular from 'angular';

/**
 * AngularJS components used for testing.
 * @namespace test
 */

/**
 * AngularJS services used in testing.
 * @namespace test.services
 */

/**
 * Mixins used in test suites.
 * @namespace test.mixins
 */

/**
 * A Webpack module for the Biobank AngularJS *test* layer.
 *
 * @memberOf test
 */
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
