/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  TestSuiteMixinFactory.$inject = ['$injector', '$templateCache'];

  function TestSuiteMixinFactory($injector, $templateCache) {

    function TestSuiteMixin() {}

    TestSuiteMixin.prototype.injectDependencies = function (/* dep1, dep2, ..., depn */) {
      var self = this;
      _.each(arguments, function (dependency) {
        self[dependency] = $injector.get(dependency);
      });
    };

    TestSuiteMixin.prototype.putHtmlTemplates = function (/* template1, template2, ... */) {
      _.each(arguments, function (template) {
        $templateCache.put(template,
                           jasmine.getFixtures().getFixtureHtml_('../../../base/app' + template));
      });
    };

    TestSuiteMixin.prototype.capitalizeFirstLetter = function(string) {
      return string.charAt(0).toUpperCase() + string.slice(1);
    };

    return TestSuiteMixin;
  }

  return TestSuiteMixinFactory;

});
