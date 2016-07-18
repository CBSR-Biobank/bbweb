/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Directive: selectStudy', function() {

    var panelHeader = 'selectStudy directive header',
        navigateStateName = 'test-navigate-state-name',
        navigateStateParamName = 'test-navigate-state-param-name';

    var getHeader = function () {
      return panelHeader;
    };

    var createScope = function (options) {
      this.element = angular.element([
        '<select-study get-header="model.getHeader"',
        '              get-studies="model.getStudies"',
        '              icon="glyphicon-ok-circle"',
        '              page-size="model.pageSize"',
        '              message-no-results="No results match the criteria."',
        '              navigate-state-name="' + navigateStateName + '"',
        '              navigate-state-param-name="' + navigateStateParamName + '">',
        '</select-study>'
      ].join(''));
      this.scope = this.$rootScope.$new();
      this.scope.model = _.extend({}, { getHeader:  getHeader }, options);
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
    };

    var createGetStudiesFn = function (studies) {
      var self = this;
      return getStudies;

      function getStudies (pagerOptions) {
        return self.$q.when({
          items:    studies.slice(0, pagerOptions.pageSize),
          page:     0,
          offset:   0,
          total:    studies.length,
          pageSize: pagerOptions.pageSize,
          maxPages: studies.length / pagerOptions.pageSize
        });
      }
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(testSuiteMixin, testUtils) {
      _.extend(this, testSuiteMixin);


      this.injectDependencies('$q', '$rootScope', '$compile', 'factory');

      this.putHtmlTemplates(
        '/assets/javascripts/collection/directives/selectStudy/selectStudy.html');
    }));

    it('displays the list of studies', function() {
      var self = this,
          studies = _.map(_.range(20), function () { return self.factory.study(); }),
          pageSize = studies.length / 2;

      createScope.call(this,
                       {
                         getStudies: createGetStudiesFn.call(self, studies),
                         pageSize: pageSize
                       });

      expect(self.element.find('li.list-group-item').length).toBe(pageSize);
      expect(self.element.find('input').length).toBe(1);
    });

    it('displays the pannel header correctly', function() {
      var self = this,
          studies = _.map(_.range(20), function () { return self.factory.study(); }),
          pageSize = studies.length / 2;

      createScope.call(this,
                       {
                         getStudies: createGetStudiesFn.call(this, studies),
                         pageSize: pageSize
                       });
      expect(self.element.find('h3').text()).toBe(panelHeader);
    });

    it('has a name filter', function() {
      var self = this,
          studies = _.map(_.range(10), function () { return self.factory.study(); });

      createScope.call(this,
                       {
                         getStudies: createGetStudiesFn.call(self, studies),
                         pageSize: studies.length
                       });
      expect(self.element.find('input').length).toBe(1);
    });

    it('displays pagination controls', function() {
      var self = this,
          studies = _.map(_.range(20), function () { return self.factory.study(); }),
          pageSize = studies.length / 2;

      createScope.call(this,
                       {
                         getStudies: createGetStudiesFn.call(self, studies),
                         pageSize: pageSize
                       });

      expect(self.element.isolateScope().vm.showPagination).toBe(true);
      expect(self.element.find('ul.pagination').length).toBe(1);
    });

    it('updates to name filter cause studies to be re-loaded', function() {
      var self = this,
          studies = _.map(_.range(20), function () { return self.factory.study(); }),
          pageSize = studies.length / 2;

      createScope.call(this,
                       {
                         getStudies: createGetStudiesFn.call(self, studies),
                         pageSize: pageSize
                       });
      spyOn(self.scope.model, 'getStudies').and.callThrough();
      self.element.isolateScope().vm.nameFilterUpdated();
      expect(this.scope.model.getStudies).toHaveBeenCalled();
    });

    it('page change studies to be re-loaded', function() {
      var self = this,
          studies = _.map(_.range(20), function () { return self.factory.study(); }),
          pageSize = studies.length / 2;

      createScope.call(this,
                       {
                         getStudies: createGetStudiesFn.call(self, studies),
                         pageSize: pageSize
                       });

      spyOn(self.scope.model, 'getStudies').and.callThrough();
      self.element.isolateScope().vm.pageChanged();
      expect(self.scope.model.getStudies).toHaveBeenCalled();
    });

    it('clear filter studies to be re-loaded', function() {
      var self = this,
          studies = _.map(_.range(20), function () { return self.factory.study(); }),
          pageSize = studies.length / 2;

      createScope.call(this,
                       {
                         getStudies: createGetStudiesFn.call(self, studies),
                         pageSize: pageSize
                       });

      spyOn(self.scope.model, 'getStudies').and.callThrough();
      self.element.isolateScope().vm.clearFilter();
      expect(self.scope.model.getStudies).toHaveBeenCalled();
    });

    it('navigateToStudyHref returns valid link', function() {
      var self = this,
          $state = this.$injector.get('$state'),
          studies = _.map(_.range(20), function () { return self.factory.study(); }),
          pageSize = studies.length / 2,
          fakeUrl = self.factory.stringNext(),
          stateNameParam = {},
          studyToNavigateTo = studies[0];

      createScope.call(this,
                       {
                         getStudies: createGetStudiesFn.call(this, studies),
                         pageSize: pageSize
                       });

      spyOn($state, 'href').and.returnValue(fakeUrl);

      expect(self.element.isolateScope().vm.navigateToStudyHref(studyToNavigateTo))
        .toEqual('<a href="' + fakeUrl + '"><strong><i class="glyphicon glyphicon-ok-circle"></i> ' +
                 studies[0].name + '</strong></a>');

      stateNameParam[navigateStateParamName] = studyToNavigateTo.id;
      expect($state.href).toHaveBeenCalledWith(
        navigateStateName,
        stateNameParam,
        { absolute: true});
    });

  });

});
