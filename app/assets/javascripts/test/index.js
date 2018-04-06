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
 * Behaviours sahred by test suites.
 * @namespace test.behaviours
 */

/**
 * Jasmine matchers used in test suites.
 * @namespace test.matchers
 */

/**
 * Mixins used in test suites.
 * @namespace test.mixins
 */

/**
 * AngularJS services used in testing.
 * @namespace test.services
 */

/**
 * A Webpack module for the Biobank AngularJS *test* layer.
 *
 * @memberOf test
 */
const ngTestModule = angular.module('biobank.test', [])

const contextList = [
  require.context('./mixins', true, /\.js$/),
  require.context('./services', true, /\.js$/)
]

contextList
  .reduce((deps, context) => (
    deps.concat(context.keys().map(context))
  ), [])
  .forEach(dep => {
    dep.default(ngTestModule)
  })

export default ngTestModule.name
