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
        '              limit="model.limit"',
        '              message-no-results="No results match the criteria."',
        '              navigate-state-name="' + navigateStateName + '"',
        '              navigate-state-param-name="' + navigateStateParamName + '">',
        '</select-study>'
      ].join(''));
      this.scope = this.$rootScope.$new();
      this.scope.model = _.extend({ getHeader:  getHeader }, options);
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('selectStudy');
    };

    var createGetStudiesFn = function (studies) {
      var self = this;
      return getStudies;

      function getStudies (pagerOptions) {
        return self.$q.when({
          items:    studies.slice(0, pagerOptions.limit),
          page:     0,
          offset:   0,
          total:    studies.length,
          limit: pagerOptions.limit,
          maxPages: studies.length / pagerOptions.limit
        });
      }
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin, testUtils) {
      _.extend(this, TestSuiteMixin.prototype);

      this.injectDependencies('$q', '$rootScope', '$compile', 'factory');
      this.putHtmlTemplates(
        '/assets/javascripts/collection/directives/selectStudy/selectStudy.html');
    }));

    it('displays the list of studies', function() {
      var self = this,
          studies = _.map(_.range(20), function () { return self.factory.study(); }),
          limit = studies.length / 2;

      createScope.call(this,
                       {
                         getStudies: createGetStudiesFn.call(self, studies),
                         limit: limit
                       });

      expect(self.element.find('li.list-group-item').length).toBe(limit);
      expect(self.element.find('input').length).toBe(1);
    });

    it('displays the pannel header correctly', function() {
      var self = this,
          studies = _.map(_.range(20), function () { return self.factory.study(); }),
          limit = studies.length / 2;

      createScope.call(this,
                       {
                         getStudies: createGetStudiesFn.call(this, studies),
                         limit: limit
                       });
      expect(self.element.find('h3').text()).toBe(panelHeader);
    });

    it('has a name filter', function() {
      var self = this,
          studies = _.map(_.range(10), function () { return self.factory.study(); });

      createScope.call(this,
                       {
                         getStudies: createGetStudiesFn.call(self, studies),
                         limit: studies.length
                       });
      expect(self.element.find('input').length).toBe(1);
    });

    it('displays pagination controls', function() {
      var self = this,
          studies = _.map(_.range(20), function () { return self.factory.study(); }),
          limit = studies.length / 2;

      createScope.call(this,
                       {
                         getStudies: createGetStudiesFn.call(self, studies),
                         limit: limit
                       });

      expect(self.controller.showPagination).toBe(true);
      expect(self.element.find('ul.pagination-sm').length).toBe(1);
    });

    it('updates to name filter cause studies to be re-loaded', function() {
      var self = this,
          studies = _.map(_.range(20), function () { return self.factory.study(); }),
          limit = studies.length / 2;

      createScope.call(this,
                       {
                         getStudies: createGetStudiesFn.call(self, studies),
                         limit: limit
                       });
      spyOn(self.scope.model, 'getStudies').and.callThrough();
      self.controller.nameFilterUpdated();
      expect(this.scope.model.getStudies).toHaveBeenCalled();
    });

    it('page change causes studies to be re-loaded', function() {
      var self = this,
          studies = _.map(_.range(20), function () { return self.factory.study(); }),
          limit = studies.length / 2;

      createScope.call(this,
                       {
                         getStudies: createGetStudiesFn.call(self, studies),
                         limit: limit
                       });

      spyOn(self.scope.model, 'getStudies').and.callThrough();
      self.controller.pageChanged();
      expect(self.scope.model.getStudies).toHaveBeenCalled();
    });

    it('clear filter causes studies to be re-loaded', function() {
      var self = this,
          studies = _.map(_.range(20), function () { return self.factory.study(); }),
          limit = studies.length / 2;

      createScope.call(this,
                       {
                         getStudies: createGetStudiesFn.call(self, studies),
                         limit: limit
                       });

      spyOn(self.scope.model, 'getStudies').and.callThrough();
      self.controller.clearFilter();
      expect(self.scope.model.getStudies).toHaveBeenCalled();
    });

    it('studyGlyphicon returns valid image tag', function() {
      var self = this,
          studies = _.map(_.range(20), function () { return self.factory.study(); }),
          limit = studies.length / 2,
          studyToNavigateTo = studies[0];

      createScope.call(this,
                       {
                         getStudies: createGetStudiesFn.call(this, studies),
                         limit: limit
                       });

      expect(self.controller.studyGlyphicon(studyToNavigateTo))
        .toEqual('<i class="glyphicon glyphicon-ok-circle"></i>');
    });

  });

});
