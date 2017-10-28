/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

/**
 * This is a mixin that can be added UserContext object of a Jasmine test suite.
 *
 * @return {object} Object containing the functions that will be mixed in.
 */
/* @ngInject */
function StateTestSuiteMixin($state, $injector, TestSuiteMixin) {

  return Object.assign({ gotoUrl, resolve }, TestSuiteMixin);

  function gotoUrl(url) {
    this.$location.url(url);
    this.$rootScope.$digest();
  }

  function resolve(value) {
    return {
      forStateAndView: function (state, view) {
        var viewDefinition = view ? $state.get(state).views[view] : $state.get(state);
        return $injector.invoke(viewDefinition.resolve[value]);
      }
    };
  }

}

export default ngModule => ngModule.service('StateTestSuiteMixin', StateTestSuiteMixin)
