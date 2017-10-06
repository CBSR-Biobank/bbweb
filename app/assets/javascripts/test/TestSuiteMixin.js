/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  TestSuiteMixinFactory.$inject = ['$injector', 'UrlService'];

  function TestSuiteMixinFactory($injector, UrlService) {

    function TestSuiteMixin() {}

    TestSuiteMixin.prototype.injectDependencies = function (/* dep1, dep2, ..., depn */) {
      var self = this;
      _.toArray(arguments).forEach((dependency) => {
        self[dependency] = $injector.get(dependency);
      });
    };

    TestSuiteMixin.prototype.capitalizeFirstLetter = function(string) {
      return string.charAt(0).toUpperCase() + string.slice(1);
    };

    TestSuiteMixin.prototype.url = function () {
      return UrlService.url.apply(UrlService, _.toArray(arguments));
    };

    return TestSuiteMixin;
  }

  return TestSuiteMixinFactory;

});
