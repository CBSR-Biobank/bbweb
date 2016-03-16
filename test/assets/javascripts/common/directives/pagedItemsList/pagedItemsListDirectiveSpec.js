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

  describe('Directive: pagedItemsListDirective', function() {

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

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (jsonEntities) {
      this.jsonEntities = jsonEntities;
    }));

    describe('Centres', function () {
      var context = {};

      beforeEach(inject(function ($q, Centre, CentreStatus) {
        var self = this,
            disabledCentres,
            enabledCentres;

        disabledCentres = _.map(_.range(2), function() {
          return new self.jsonEntities.centre();
        });
        enabledCentres = _.map(_.range(3), function() {
          var centre = new self.jsonEntities.centre();
          centre.status = CentreStatus.ENABLED();
          return centre;
        });

        context.entities                     = disabledCentres.concat(enabledCentres);
        context.counts                       = createCounts(disabledCentres.length,
                                                            enabledCentres.length);
        context.pageSize                     = disabledCentres.length;
        context.messageNoItems               = 'No items present';
        context.messageNoResults             = 'No items match the criteria';
        context.entityNavigateState          = 'home.admin.centres.centre.summary';
        context.entityNavigateStateParamName = 'centreId';

        context.possibleStatuses = [{ id: 'all', name: 'all' }].concat(
          _.map(CentreStatus.values(), function (status) {
            return { id: status, name: CentreStatus.label(status) };
          }));
      }));

      sharedBehaviour(context);
    });

    describe('Studies', function () {
      var context = {};

      beforeEach(inject(function ($q, Study, StudyStatus) {
        var self = this,
            disabledStudies,
            enabledStudies,
            retiredStudies;

        disabledStudies = _.map(_.range(2), function() {
          return new self.jsonEntities.study();
        });
        enabledStudies = _.map(_.range(3), function() {
          var study = new self.jsonEntities.study();
          study.status = StudyStatus.ENABLED();
          return study;
        });
        retiredStudies = _.map(_.range(3), function() {
          var study = new self.jsonEntities.study();
          study.status = StudyStatus.RETIRED();
          return study;
        });

        context.entities                     = disabledStudies.concat(enabledStudies.concat(retiredStudies));
        context.counts                       = createCounts(disabledStudies.length,
                                                            enabledStudies.length,
                                                            retiredStudies.length);
        context.pageSize                     = disabledStudies.length;
        context.messageNoItems               = 'No items present';
        context.messageNoResults             = 'No items match the criteria';
        context.entityNavigateState          = 'home.admin.studies.study.summary';
        context.entityNavigateStateParamName = 'studyId';

        context.possibleStatuses = [{ id: 'all', name: 'all' }].concat(
          _.map(StudyStatus.values(), function (status) {
            return { id: status, name: StudyStatus.label(status) };
          }));
      }));

      sharedBehaviour(context);
    });

    function sharedBehaviour(context) {

      describe('(shared)', function () {

        beforeEach(inject(function (directiveTestSuite, testUtils) {
          _.extend(this, directiveTestSuite);

          this.$q                     = this.$injector.get('$q');
          this.context                = context;
          this.createController       = setupController(this);
          this.getItemsSpy            = jasmine.createSpy('getItemsSpy');
          this.getItemsWrapperDefault = createDefaultGetItemsWrapper(this);

          this.putHtmlTemplates(
            '/assets/javascripts/common/directives/pagedItemsList/pagedItemsList.html');

          testUtils.addCustomMatchers();
        }));

        function setupController(userContext) {
          var $rootScope = userContext.$injector.get('$rootScope'),
              $compile   = userContext.$injector.get('$compile');

          return create;

          //--

          function create(getItemsWrapper) {
            var element = angular.element([
              '<paged-items-list',
              '  counts="vm.counts"',
              '  page-size="vm.pageSize"',
              '  possible-statuses="vm.possibleStatuses"',
              '  message-no-items="' + userContext.context.messageNoItems + '"',
              '  message-no-results="' + userContext.context.messageNoResults + '"',
              '  get-items="vm.getItems"',
              '  entity-navigate-state="' + userContext.context.entityNavigateState + '"',
              '  entity-navigate-state-param-name="' + userContext.context.entityNavigateStateParamName + '">',
              '</paged-items-list>'
            ].join(''));

            userContext.scope = $rootScope.$new();
            userContext.scope.vm = {
              counts:           userContext.context.counts,
              pageSize:         userContext.context.pageSize,
              possibleStatuses: userContext.context.possibleStatuses,
              getItems:         getItemsWrapper
            };

            $compile(element)(userContext.scope);
            userContext.scope.$digest();
            userContext.controller = element.controller('pagedItemsList');
          }
        }

        function createDefaultGetItemsWrapper(userContext) {
          return get;

          function get(options) {
            userContext.getItemsSpy(options);
            return userContext.$q.when({
              items:    userContext.context.entities.slice(0, userContext.pageSize),
              page:     options.page,
              pageSize: userContext.pageSize,
              offset:   0,
              total:    userContext.context.entities.length
            });
          }
        }

        it('has valid scope', function() {
          this.createController(this.getItemsWrapperDefault);

          expect(this.controller.counts).toBe(this.context.counts);
          expect(this.controller.possibleStatuses).toBe(this.context.possibleStatuses);
          expect(this.controller.sortFields).toContainAll(['Name', 'Status']);

          expect(this.controller.pagerOptions.filter).toBeEmptyString();
          expect(this.controller.pagerOptions.status).toBe(this.context.possibleStatuses[0].id);
          expect(this.controller.pagerOptions.pageSize).toBe(this.context.pageSize);
          expect(this.controller.pagerOptions.sort).toBe('name');
        });

        it('has a valid panel heading', function() {
          var self = this;

          self.createController(self.getItemsWrapperDefault);

          _.each(self.context.possibleStatuses, function (status) {
            if (status.id !== 'all') {
              expect(self.controller.panelHeading).toContain(status.name);
            }
          });
        });

        it('updates items when name filter is updated', function() {
          var nameFilterValue = 'test';

          this.createController(this.getItemsWrapperDefault);

          this.controller.pagerOptions.filter = nameFilterValue;
          this.controller.nameFilterUpdated();
          this.scope.$digest();
          expect(this.getItemsSpy.calls.mostRecent().args[0]).toEqual({
            filter: nameFilterValue,
            status: this.context.possibleStatuses[0].id,
            page: 1,
            pageSize: this.context.pageSize,
            sort: 'name'
          });
        });

        it('updates items when name status filter is updated', function() {
          var statusFilterValue = this.context.possibleStatuses[1];

          this.createController(this.getItemsWrapperDefault);
          this.controller.pagerOptions.status = statusFilterValue;
          this.controller.statusFilterUpdated();
          this.scope.$digest();
          expect(this.getItemsSpy.calls.mostRecent().args[0]).toEqual({
            filter: '',
            status: statusFilterValue,
            page: 1,
            pageSize: this.context.pageSize,
            sort: this.controller.sortFields[0].toLowerCase()
          });
        });

        it('clears filters', function() {
          this.createController(this.getItemsWrapperDefault);
          this.controller.pagerOptions.filter = 'test';
          this.controller.pagerOptions.status = this.context.possibleStatuses[1];
          this.controller.clearFilters();
          this.scope.$digest();
          expect(this.controller.pagerOptions.filter).toBeNull();
          expect(this.controller.pagerOptions.status).toBe(this.context.possibleStatuses[0]);
        });

        it('updates items when name sort field is updated', function() {
          var sortFieldValue;

          this.createController(this.getItemsWrapperDefault);
          sortFieldValue = this.controller.sortFields[1];
          this.controller.sortFieldSelected(sortFieldValue);
          this.scope.$digest();
          expect(this.getItemsSpy.calls.mostRecent().args[0]).toEqual({
            filter: '',
            status: this.context.possibleStatuses[0].id,
            page: 1,
            pageSize: this.context.pageSize,
            sort: sortFieldValue.toLowerCase()
          });
        });

        it('updates items when name page number is changed', function() {
          var page = 2;

          this.createController(this.getItemsWrapperDefault);
          this.controller.pagerOptions.page = page;
          this.controller.pageChanged();
          this.scope.$digest();
          expect(this.getItemsSpy.calls.mostRecent().args[0]).toEqual({
            filter: '',
            status: this.context.possibleStatuses[0].id,
            page: page,
            pageSize: this.context.pageSize,
            sort: this.controller.sortFields[0].toLowerCase()
          });
        });

        it('has valid display state when there are no entities for criteria', function() {
          var self = this;

          this.createController(getItemsWrapper);
          expect(this.controller.displayState).toBe(1); // NO_RESULTS

          function getItemsWrapper(options) {
            self.getItemsSpy(options);
            return self.$q.when({
              items:    [],
              page:     options.page,
              pageSize: self.context.pageSize,
              offset:   0,
              total:    0
            });
          }
        });

        it('has valid display state when there are entities for criteria', function() {
          var self = this;

          this.createController(getItemsWrapper);
          expect(self.controller.displayState).toBe(2); // HAVE_RESULTS

          function getItemsWrapper(options) {
            self.getItemsSpy(options);
            return self.$q.when({
              items:    self.context.entities.slice(0, self.context.pageSize),
              page:     options.page,
              pageSize: self.context.pageSize,
              offset:   0,
              total:    self.context.entities.length + 1
            });
          }
        });

        it('has valid display state when there are no entities', function() {
          this.context.counts = _.mapObject(this.context.counts, function (val) {
            return 0;
          });
          this.createController(this.getItemsWrapperDefault);
          expect(this.controller.displayState).toBe(0); // NO_ENTITIES
        });

      });

    }

  });

});
