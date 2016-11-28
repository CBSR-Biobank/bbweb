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

  describe('Directive: pagedItemsListDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (factory) {
      this.factory = factory;
    }));

    describe('Centres', function () {
      var context = {};

      beforeEach(inject(function ($q, Centre, CentreState) {
        var self = this,
            disabledCentres,
            enabledCentres;

        disabledCentres = _.map(_.range(2), function() {
          return new self.factory.centre();
        });
        enabledCentres = _.map(_.range(3), function() {
          var centre = new self.factory.centre();
          centre.state = CentreState.ENABLED;
          return centre;
        });

        context.entities                     = disabledCentres.concat(enabledCentres);
        context.counts                       = createCounts(disabledCentres.length,
                                                            enabledCentres.length);
        context.limit                        = disabledCentres.length;
        context.messageNoItems               = 'No items present';
        context.messageNoResults             = 'No items match the criteria';
        context.entityNavigateState          = 'home.admin.centres.centre.summary';
        context.entityNavigateStateParamName = 'centreId';

        context.possibleStates = [{ id: 'all', name: 'all' }].concat(
          _.map(_.values(CentreState), function (state) {
            return { id: state, name: state.toUpperCase() };
          }));
      }));

      sharedBehaviour(context);
    });

    describe('Studies', function () {
      var context = {};

      beforeEach(inject(function ($q, Study, StudyState) {
        var self = this,
            disabledStudies,
            enabledStudies,
            retiredStudies;

        disabledStudies = _.map(_.range(2), function() {
          return new self.factory.study();
        });
        enabledStudies = _.map(_.range(3), function() {
          var study = new self.factory.study();
          study.state = StudyState.ENABLED;
          return study;
        });
        retiredStudies = _.map(_.range(3), function() {
          var study = new self.factory.study();
          study.state = StudyState.RETIRED;
          return study;
        });

        context.entities                     = disabledStudies.concat(enabledStudies.concat(retiredStudies));
        context.counts                       = createCounts(disabledStudies.length,
                                                            enabledStudies.length,
                                                            retiredStudies.length);
        context.limit                        = disabledStudies.length;
        context.messageNoItems               = 'No items present';
        context.messageNoResults             = 'No items match the criteria';
        context.entityNavigateState          = 'home.admin.studies.study.summary';
        context.entityNavigateStateParamName = 'studyId';

        context.possibleStates = [{ id: 'all', name: 'all' }].concat(
          _.map(_.values(StudyState), function (state) {
            return { id: state, name: state.toUpperCase() };
          }));
      }));

      sharedBehaviour(context);
    });

    function sharedBehaviour(context) {

      describe('(shared)', function () {

        var createController = function (getItemsFunc, getItemIconFunc) {
          var self = this,
              element = angular.element([
                '<paged-items-list',
                '  counts="vm.counts"',
                '  limit="vm.limit"',
                '  possible-states="vm.possibleStates"',
                '  message-no-items="' + context.messageNoItems + '"',
                '  message-no-results="' + context.messageNoResults + '"',
                '  get-items="vm.getItems"',
                '  get-item-icon="vm.getItemIcon"',
                '  entity-navigate-state="' + context.entityNavigateState + '"',
                '  entity-navigate-state-param-name="' + context.entityNavigateStateParamName + '">',
                '</paged-items-list>'
          ].join(''));

          getItemsFunc = getItemsFunc || getItemsFuncDefault;
          getItemIconFunc = getItemIconFunc || function (entity) { return ''; };

          self.scope = self.$rootScope.$new();
          self.scope.vm = {
            counts:         context.counts,
            limit:          context.limit,
            possibleStates: context.possibleStates,
            getItems:       getItemsFunc,
            getItemIcon:    getItemIconFunc
          };

          self.$compile(element)(self.scope);
          self.scope.$digest();
          self.controller = element.controller('pagedItemsList');

          function getItemsFuncDefault (options) {
            self.getItemsSpy(options);
            return self.$q.when({
              items:    context.entities.slice(0, self.limit),
              page:     options.page,
              limit: self.limit,
              offset:   0,
              total:    context.entities.length
            });
          }
        };

        beforeEach(inject(function ($rootScope, $compile, TestSuiteMixin, testUtils) {
          var self = this;

          _.extend(self, TestSuiteMixin.prototype);

          self.injectDependencies('$rootScope', '$compile', '$q');
          self.context     = context;
          self.getItemsSpy = jasmine.createSpy('getItemsSpy');

          self.putHtmlTemplates(
            '/assets/javascripts/common/directives/pagedItemsList/pagedItemsList.html');

          testUtils.addCustomMatchers();
        }));

        it('has valid scope', function() {
          createController.call(this);

          expect(this.controller.counts).toBe(this.context.counts);
          expect(this.controller.possibleStates).toBe(this.context.possibleStates);
          expect(this.controller.sortFields).toContainAll(['Name', 'State']);

          expect(this.controller.nameFilter).toBeEmptyString();
          expect(this.controller.selectedState).toBe('all');
          expect(this.controller.pagerOptions.limit).toBe(this.context.limit);
          expect(this.controller.pagerOptions.sort).toBe('name');
        });

        it('has a valid panel heading', function() {
          var self = this;

          createController.call(self);

          _.each(context.possibleStates, function (state) {
            if (state.id !== 'all') {
              expect(self.controller.panelHeading.toLowerCase()).toContain(state.name.toLowerCase());
            }
          });
        });

        it('updates items when name filter is updated', function() {
          var nameFilterValue = 'test';

          createController.call(this);

          this.controller.nameFilter = nameFilterValue;
          this.controller.nameFilterUpdated();
          this.scope.$digest();
          expect(this.getItemsSpy.calls.mostRecent().args[0]).toEqual({
            filter: 'name::*' + nameFilterValue + '*',
            sort: 'name',
            page: 1,
            limit: this.context.limit
          });
        });

        it('updates items when name state filter is updated', function() {
          var statusFilterValue = this.context.possibleStates[1];

          createController.call(this);
          this.controller.pagerOptions.state = statusFilterValue;
          this.controller.stateFilterUpdated();
          this.scope.$digest();
          expect(this.getItemsSpy.calls.mostRecent().args[0]).toEqual({
            filter: '',
            state: statusFilterValue,
            page: 1,
            limit: this.context.limit,
            sort: this.controller.sortFields[0].toLowerCase()
          });
        });

        it('clears filters', function() {
          createController.call(this);
          this.controller.pagerOptions.filter = 'test';
          this.controller.pagerOptions.state = this.context.possibleStates[1];
          this.controller.clearFilters();
          this.scope.$digest();
          expect(this.controller.pagerOptions.filter).toBeEmptyString();
        });

        it('updates items when name sort field is updated', function() {
          var sortFieldValue;

          createController.call(this);
          sortFieldValue = this.controller.sortFields[1];
          this.controller.sortFieldSelected(sortFieldValue);
          this.scope.$digest();
          expect(this.getItemsSpy.calls.mostRecent().args[0]).toEqual({
            filter: '',
            sort: sortFieldValue.toLowerCase(),
            page: 1,
            limit: this.context.limit
          });
        });

        it('updates items when name page number is changed', function() {
          var page = 2;

          createController.call(this);
          this.controller.pagerOptions.page = page;
          this.controller.pageChanged();
          this.scope.$digest();
          expect(this.getItemsSpy.calls.mostRecent().args[0]).toEqual({
            filter: '',
            sort: this.controller.sortFields[0].toLowerCase(),
            page: page,
            limit: this.context.limit
          });
        });

        it('has valid display state when there are no entities for criteria', function() {
          var self = this;

          createController.call(this, getItemsWrapper);
          expect(this.controller.displayState).toBe(1); // NO_RESULTS

          function getItemsWrapper(options) {
            self.getItemsSpy(options);
            return self.$q.when({
              items:    [],
              page:     options.page,
              limit: context.limit,
              offset:   0,
              total:    0
            });
          }
        });

        it('has valid display state when there are entities for criteria', function() {
          var self = this;

          createController.call(this, getItemsWrapper);
          expect(self.controller.displayState).toBe(2); // HAVE_RESULTS

          function getItemsWrapper(options) {
            self.getItemsSpy(options);
            return self.$q.when({
              items:    context.entities.slice(0, context.limit),
              page:     options.page,
              limit: context.limit,
              offset:   0,
              total:    context.entities.length + 1
            });
          }
        });

        it('has valid display state when there are no entities', function() {
          this.context.counts = _.mapValues(this.context.counts, function (val) {
            return 0;
          });
          createController.call(this);
          expect(this.controller.displayState).toBe(0); // NO_ENTITIES
        });

      });

    }

    function createCounts(/* disabled, enabled, retired */) {
      var args = _.toArray(arguments),
          result = {};

      if (args.length < 1) {
        throw new Error('disabled count not specified');
      }

      result.disabled = args.shift();
      result.total = result.disabled;

      if (args.length < 1) {
        throw new Error('enabled count not specified');
      }

      result.enabled = args.shift();
      result.total += result.enabled;

      if (args.length > 0) {
        result.retired = args.shift();
        result.total += result.retired;
      }

      return result;
    }

  });

});
