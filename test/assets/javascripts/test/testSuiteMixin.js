/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function (_) {
  'use strict';

  testSuiteMixin.$inject = ['$templateCache'];

  function testSuiteMixin($templateCache) {

    var mixin = {
      injectDependencies: injectDependencies,
      putHtmlTemplates: putHtmlTemplates
    };

    return mixin;

    //---

    function injectDependencies(/* dep1, dep2, ..., depn */) {
      /*jshint validthis:true */
      var self = this;
      _.each(arguments, function (dependency) {
        self[dependency] = self.$injector.get(dependency);
      });
    }

    function putHtmlTemplates(/* template1, template2, ... */) {
      _.each(arguments, function (template) {
        $templateCache.put(template,
                           jasmine.getFixtures().getFixtureHtml_('../../../base/app' + template));
      });
    }
  }

  return testSuiteMixin;

});
