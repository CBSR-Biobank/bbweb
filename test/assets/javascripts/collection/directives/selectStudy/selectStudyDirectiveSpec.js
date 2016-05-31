/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Directive: selectStudy', function() {

    var scope,
        q,
        compile,
        element,
        state,
        factory,
        panelHeader = 'selectStudy directive header',
        navigateStateName = 'test-navigate-state-name',
        navigateStateParamName = 'test-navigate-state-param-name';

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(templateMixin, testUtils) {
      _.extend(this, templateMixin);

      q            = this.$injector.get('$q');
      compile      = this.$injector.get('$compile');
      scope        = this.$injector.get('$rootScope');
      state        = this.$injector.get('$state');
      factory = this.$injector.get('factory');

      this.putHtmlTemplates(
        '/assets/javascripts/collection/directives/selectStudy/selectStudy.html');

      element = generateElement(navigateStateName, navigateStateParamName);
    }));

    function generateElement(navigateStateName, navigateStateParamName) {
      return angular.element(
        '<select-study get-header="model.getHeader"' +
          '            get-studies="model.getStudies"' +
          '            icon="glyphicon-ok-circle"' +
          '            page-size="model.pageSize"' +
          '            messageNoResults="No results match the criteria."' +
          '            navigate-state-name="' + navigateStateName + '"' +
          '            navigate-state-param-name="' + navigateStateParamName + '">');
    }

    function getHeader() {
      return panelHeader;
    }

    function createScope(options) {
      scope.model = _.extend({}, { getHeader:  getHeader }, options);
      compile(element)(scope);
      scope.$digest();
      return scope;
    }

    function createGetStudiesFn(studies) {
      return getStudies;

      function getStudies (pagerOptions) {
        return q.when({
          items:    studies.slice(0, pagerOptions.pageSize),
          page:     0,
          offset:   0,
          total:    studies.length,
          pageSize: pagerOptions.pageSize,
          maxPages: studies.length / pagerOptions.pageSize
        });
      }
    }

    it('displays the list of studies', function() {
      var studies = _.map(_.range(20), function () { return factory.study(); }),
          pageSize = studies.length / 2;

      createScope({
        getStudies: createGetStudiesFn(studies),
        pageSize: pageSize
      });

      expect(element.find('li.list-group-item').length).toBe(pageSize);
      expect(element.find('input').length).toBe(1);
    });

    it('displays the pannel header correctly', function() {
      var studies = _.map(_.range(20), function () { return factory.study(); }),
          pageSize = studies.length / 2;

      createScope({
        getStudies: createGetStudiesFn(studies),
        pageSize: pageSize
      });
      expect(element.find('h3').text()).toBe(panelHeader);
    });

    it('has a name filter', function() {
      var studies = _.map(_.range(10), function () { return factory.study(); });

      createScope({
        getStudies: createGetStudiesFn(studies),
        pageSize: studies.length
      });
      expect(element.find('input').length).toBe(1);
    });

    it('displays pagination controls', function() {
      var studies = _.map(_.range(20), function () { return factory.study(); }),
          pageSize = studies.length / 2,
          scope;

      scope = createScope({
        getStudies: createGetStudiesFn(studies),
        pageSize: pageSize
      });

      expect(element.isolateScope().vm.showPagination).toBe(true);
      expect(element.find('ul.pagination').length).toBe(1);
    });

    it('updates to name filter cause studies to be re-loaded', function() {
      var studies = _.map(_.range(20), function () { return factory.study(); }),
          pageSize = studies.length / 2,
          scope = createScope({
            getStudies: createGetStudiesFn(studies),
            pageSize: pageSize
          });

      spyOn(scope.model, 'getStudies').and.callThrough();
      element.isolateScope().vm.nameFilterUpdated();
      expect(scope.model.getStudies).toHaveBeenCalled();
    });

    it('page change studies to be re-loaded', function() {
      var studies = _.map(_.range(20), function () { return factory.study(); }),
          pageSize = studies.length / 2,
          scope = createScope({
            getStudies: createGetStudiesFn(studies),
            pageSize: pageSize
          });

      spyOn(scope.model, 'getStudies').and.callThrough();
      element.isolateScope().vm.pageChanged();
      expect(scope.model.getStudies).toHaveBeenCalled();
    });

    it('clear filter studies to be re-loaded', function() {
      var studies = _.map(_.range(20), function () { return factory.study(); }),
          pageSize = studies.length / 2,
          scope = createScope({
            getStudies: createGetStudiesFn(studies),
            pageSize: pageSize
          });

      spyOn(scope.model, 'getStudies').and.callThrough();
      element.isolateScope().vm.clearFilter();
      expect(scope.model.getStudies).toHaveBeenCalled();
    });

    it('navigateToStudyHref returns valid link', function() {
      var studies = _.map(_.range(20), function () { return factory.study(); }),
          pageSize = studies.length / 2,
          fakeUrl = factory.stringNext(),
          stateNameParam = {},
          studyToNavigateTo = studies[0];

      createScope({
        getStudies: createGetStudiesFn(studies),
        pageSize: pageSize
      });

      spyOn(state, 'href').and.callFake(function () { return  fakeUrl; });

      expect(element.isolateScope().vm.navigateToStudyHref(studyToNavigateTo))
        .toEqual('<a href="' + fakeUrl + '"><strong><i class="glyphicon glyphicon-ok-circle"></i> ' +
                 studies[0].name + '</strong></a>');

      stateNameParam[navigateStateParamName] = studyToNavigateTo.id;
      expect(state.href).toHaveBeenCalledWith(
        navigateStateName,
        stateNameParam,
        { absolute: true});
    });

  });

});
