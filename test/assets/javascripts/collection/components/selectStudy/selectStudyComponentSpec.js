/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('Component: selectStudy', function() {

    var panelHeader = 'selectStudy component header',
        navigateStateName = 'test-navigate-state-name',
        navigateStateParamName = 'test-navigate-state-param-name',
        getHeader = function () {
          return panelHeader;
        };

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function (scopeVars) {
        ComponentTestSuiteMixin.prototype.createScope.call(
          this,
          [
             '<select-study get-header="vm.getHeader"',
             '              get-studies="vm.getStudies"',
             '              icon="glyphicon-ok-circle"',
             '              limit="vm.limit"',
             '              message-no-results="No results match the criteria."',
             '              navigate-state-name="' + navigateStateName + '"',
             '              navigate-state-param-name="' + navigateStateParamName + '">',
             '</select-study>'
          ].join(''),
          _.extend({ getHeader:  getHeader }, scopeVars),
          'selectStudy');
      };

      SuiteMixin.prototype.createStudies = function (numStudies) {
        var self = this;
        return _.map(_.range(numStudies), function () {
          return self.Study.create(self.factory.study());
        });
      };

      SuiteMixin.prototype.createGetStudiesFn = function (studies) {
        var self = this;
        return function (pagerOptions) {
          return self.$q.when(_.extend(self.factory.pagedResult(studies, pagerOptions),
                                       { items: studies.slice(0, pagerOptions.limit) }));
        };
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$q', '$rootScope', '$compile', 'Study', 'factory');
      this.putHtmlTemplates(
        '/assets/javascripts/collection/components/selectStudy/selectStudy.html');
    }));

    it('displays the list of studies', function() {
      var self = this,
          studies = this.createStudies(20),
          limit = studies.length / 2;

      this.createController({
        getStudies: self.createGetStudiesFn(studies),
        limit: limit
      });

      expect(self.element.find('li.list-group-item').length).toBe(limit);
      expect(self.element.find('input').length).toBe(1);
    });

    it('displays the pannel header correctly', function() {
      var self = this,
          studies = this.createStudies(20),
          limit = studies.length / 2;

      this.createController({
        getStudies: self.createGetStudiesFn(studies),
        limit: limit
      });
      expect(self.element.find('h3').text()).toBe(panelHeader);
    });

    it('has a name filter', function() {
      var self = this,
          studies = this.createStudies(20);

      this.createController({
        getStudies: this.createGetStudiesFn(studies),
        limit: studies.length
      });
      expect(self.element.find('input').length).toBe(1);
    });

    it('displays pagination controls', function() {
      var self = this,
          studies = this.createStudies(20),
          limit = studies.length / 2;

      this.createController({
        getStudies: this.createGetStudiesFn(studies),
         limit: limit
      });

      expect(self.controller.showPagination).toBe(true);
      expect(self.element.find('ul.pagination-sm').length).toBe(1);
    });

    it('updates to name filter cause studies to be re-loaded', function() {
      var self = this,
          studies = this.createStudies(20),
          limit = studies.length / 2;

      this.createController({
         getStudies: this.createGetStudiesFn(studies),
         limit: limit
      });

      spyOn(this.controller, 'getStudies').and.callThrough();

      _.forEach([
        { callCount: 1, nameFilter: 'test' },
        { callCount: 2, nameFilter: '' }
      ], function (obj) {
        self.controller.nameFilter = obj.nameFilter;
        self.controller.nameFilterUpdated();
        expect(self.controller.getStudies.calls.count()).toBe(obj.callCount);
      });
    });

    it('page change causes studies to be re-loaded', function() {
      var self = this,
          studies = this.createStudies(20),
          limit = studies.length / 2;

      this.createController({
         getStudies: this.createGetStudiesFn(studies),
         limit: limit
      });

      spyOn(self.controller, 'getStudies').and.callThrough();
      self.controller.pageChanged();
      expect(self.controller.getStudies).toHaveBeenCalled();
    });

    it('clear filter causes studies to be re-loaded', function() {
      var self = this,
          studies = this.createStudies(20),
          limit = studies.length / 2;

      this.createController({
         getStudies: this.createGetStudiesFn(studies),
         limit: limit
      });

      spyOn(self.controller, 'getStudies').and.callThrough();
      self.controller.clearFilter();
      expect(self.controller.getStudies).toHaveBeenCalled();
    });

    it('studyGlyphicon returns valid image tag', function() {
      var self = this,
          studies = this.createStudies(20),
          limit = studies.length / 2,
          studyToNavigateTo = studies[0];

      this.createController({
         getStudies: this.createGetStudiesFn(studies),
         limit: limit
      });

      expect(self.controller.studyGlyphicon(studyToNavigateTo))
        .toEqual('<i class="glyphicon glyphicon-ok-circle"></i>');
    });

    describe('when selecting a study', function() {

      beforeEach(function() {
        this.injectDependencies('$state', 'Study');
        this.study = new this.Study(this.factory.study());
      });

      it('a state change is triggered when a study is selected', function() {
        var location = this.factory.location(),
            centre = this.factory.centre({ locations: [ location ] }),
            centreLocations = this.factory.centreLocations([ centre ]),
            args;

        spyOn(this.$state, 'go').and.returnValue(null);
        spyOn(this.Study.prototype, 'allLocations').and.returnValue(this.$q.when(centreLocations));

        this.createController({
           getStudies: this.createGetStudiesFn([]),
           limit: 1
        });

        this.controller.studySelected(this.study);
        this.scope.$digest();
        expect(this.$state.go).toHaveBeenCalled();

        args = this.$state.go.calls.argsFor(0);
        expect(args[0]).toEqual(navigateStateName);
        expect(args[1][navigateStateParamName]).toEqual(this.study.id);
      });

      it('when the selected study does not have centres associated with it', function() {
        this.injectDependencies('modalService');

        spyOn(this.Study.prototype, 'allLocations').and.returnValue(this.$q.when([]));
        spyOn(this.modalService, 'modalOk').and.returnValue(this.$q.when('OK'));

        this.createController({
           getStudies: this.createGetStudiesFn([]),
           limit: 1
        });

        this.controller.studySelected(this.study);
        this.scope.$digest();
        expect(this.modalService.modalOk).toHaveBeenCalled();
      });


    });

  });

});
