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
  'biobank.testUtils',
  'biobankApp'
], function(angular, mocks, _, testUtils) {
  'use strict';

  describe('Directive: pagedItemsListDirective', function() {
    var fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(fakeDomainEntities) {
      fakeEntities = fakeDomainEntities;
      testUtils.addCustomMatchers();
    }));

    describe('Centres', function () {
      var context = {};

      beforeEach(inject(function ($q, Centre, CentreStatus) {
        var disabledCentres, enabledCentres;

        disabledCentres = _.map(_.range(2), function() {
          return new fakeEntities.centre();
        });
        enabledCentres = _.map(_.range(3), function() {
          var centre = new fakeEntities.centre();
          centre.status = CentreStatus.ENABLED();
          return centre;
        });

        context.entities =
          disabledCentres.concat(enabledCentres);
        context.counts =
          createCounts(disabledCentres.length, enabledCentres.length);
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

      function createCounts(disabled, enabled) {
        return {
          total:    disabled + enabled,
          disabled: disabled,
          enabled:  enabled
        };
      }

      sharedBehaviour(context);
    });

    describe('Studies', function () {
      var context = {};

      beforeEach(inject(function ($q, Study, StudyStatus) {
        var disabledStudies, enabledStudies, retiredStudies;

        disabledStudies = _.map(_.range(2), function() {
          return new fakeEntities.study();
        });
        enabledStudies = _.map(_.range(3), function() {
          var study = new fakeEntities.study();
          study.status = StudyStatus.ENABLED();
          return study;
        });
        retiredStudies = _.map(_.range(3), function() {
          var study = new fakeEntities.study();
          study.status = StudyStatus.RETIRED();
          return study;
        });

        context.entities =
          disabledStudies.concat(enabledStudies.concat(retiredStudies));
        context.counts =
          createCounts(disabledStudies.length,
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

      function createCounts(disabled, enabled, retired) {
        return {
          total:    disabled + enabled,
          disabled: disabled,
          enabled:  enabled,
          retired:  retired
        };
      }

      sharedBehaviour(context);
    });

    function sharedBehaviour(context) {
      var $q,
          scope,
          createController,
          entities,
          counts,
          pageSize,
          possibleStatuses,
          messageNoItems,
          messageNoResults,
          entityNavigateState,
          entityNavigateStateParamName,
          getItemsSpy = jasmine.createSpy('getItemsSpy');

      describe('(shared)', function () {

        beforeEach(inject(function (_$q_, $templateCache) {
          $q                           = _$q_;
          entities                     = context.entities;
          counts                       = context.counts;
          pageSize                     = context.pageSize;
          possibleStatuses             = context.possibleStatuses;
          messageNoItems               = context.messageNoItems;
          messageNoResults             = context.messageNoResults;
          entityNavigateState          = context.entityNavigateState;
          entityNavigateStateParamName = context.entityNavigateStateParamName;

          testUtils.putHtmlTemplates(
            $templateCache,
            '/assets/javascripts/common/directives/pagedItemsList/pagedItemsList.html');

          createController             = setupController(this.$injector);
        }));

        function setupController(injector) {
          var $rootScope = injector.get('$rootScope'),
              $compile   = injector.get('$compile');

          return create;

          //--

          function create(getItemsWrapper) {
            var element;

            scope = $rootScope.$new();

            element = angular.element([
              '<paged-items-list',
              '  counts="vm.counts"',
              '  page-size="vm.pageSize"',
              '  possible-statuses="vm.possibleStatuses"',
              '  message-no-items="' + messageNoItems + '"',
              '  message-no-results="' + messageNoResults + '"',
              '  get-items="vm.getItems"',
              '  entity-navigate-state="' + entityNavigateState + '"',
              '  entity-navigate-state-param-name="' + entityNavigateStateParamName + '">',
              '</paged-items-list>'
            ].join(''));

            scope.vm = {
              counts:           counts,
              pageSize:         pageSize,
              possibleStatuses: possibleStatuses,
              getItems:         getItemsWrapper
            };

            $compile(element)(scope);
            scope.$digest();
            return element.controller('pagedItemsList');
          }
        }

        function getItemsWrapperDefault(options) {
          getItemsSpy(options);
          return $q.when({
            items:    entities.slice(0, pageSize),
            page:     options.page,
            pageSize: pageSize,
            offset:   0,
            total:    entities.length
          });
        }

        it('has valid scope', function() {
          var controller = createController(getItemsWrapperDefault);

          expect(controller.counts).toBe(counts);
          expect(controller.possibleStatuses).toBe(possibleStatuses);
          expect(controller.sortFields).toContainAll(['Name', 'Status']);

          expect(controller.pagerOptions.filter).toBeEmptyString();
          expect(controller.pagerOptions.status).toBe(possibleStatuses[0].id);
          expect(controller.pagerOptions.pageSize).toBe(pageSize);
          expect(controller.pagerOptions.sortField).toBe('name');
        });

        it('has a valid panel heading', function() {
          var controller = createController(getItemsWrapperDefault);
          _.each(possibleStatuses, function (status) {
            if (status.id !== 'all') {
              expect(controller.panelHeading).toContain(status.name);
            }
          });
        });

        it('updates items when name filter is updated', function() {
          var controller = createController(getItemsWrapperDefault),
              nameFilterValue = 'test';

          controller.pagerOptions.filter = nameFilterValue;
          controller.nameFilterUpdated();
          scope.$digest();
          expect(getItemsSpy.calls.mostRecent().args[0]).toEqual({
            filter: nameFilterValue,
            status: possibleStatuses[0].id,
            page: 1,
            pageSize: pageSize,
            sortField: 'name'
          });
        });

        it('updates items when name status filter is updated', function() {
          var controller = createController(getItemsWrapperDefault),
              statusFilterValue = possibleStatuses[1];

          controller.pagerOptions.status = statusFilterValue;
          controller.statusFilterUpdated();
          scope.$digest();
          expect(getItemsSpy.calls.mostRecent().args[0]).toEqual({
            filter: '',
            status: statusFilterValue,
            page: 1,
            pageSize: pageSize,
            sortField: controller.sortFields[0].toLowerCase()
          });
        });

        it('clears filters', function() {
          var controller = createController(getItemsWrapperDefault);

          controller.pagerOptions.filter = 'test';
          controller.pagerOptions.status = possibleStatuses[1];
          controller.clearFilters();
          scope.$digest();
          expect(controller.pagerOptions.filter).toBeNull();
          expect(controller.pagerOptions.status).toBe(possibleStatuses[0]);
        });

        it('updates items when name sort field is updated', function() {
          var controller = createController(getItemsWrapperDefault),
              sortFieldValue = controller.sortFields[1];

          controller.sortFieldSelected(sortFieldValue);
          scope.$digest();
          expect(getItemsSpy.calls.mostRecent().args[0]).toEqual({
            filter: '',
            status: possibleStatuses[0].id,
            page: 1,
            pageSize: pageSize,
            sortField: sortFieldValue.toLowerCase()
          });
        });

        it('updates items when name page number is changed', function() {
          var controller = createController(getItemsWrapperDefault),
              page = 2;

          controller.pagerOptions.page = page;
          controller.pageChanged();
          scope.$digest();
          expect(getItemsSpy.calls.mostRecent().args[0]).toEqual({
            filter: '',
            status: possibleStatuses[0].id,
            page: page,
            pageSize: pageSize,
            sortField: controller.sortFields[0].toLowerCase()
          });
        });

        it('has valid display state when there are no entities for criteria', function() {
          var controller = createController(getItemsWrapper);

          expect(controller.displayState).toBe(1); // NO_RESULTS

          function getItemsWrapper(options) {
            getItemsSpy(options);
            return $q.when({
              items:    entities.slice(0, pageSize),
              page:     options.page,
              pageSize: pageSize,
              offset:   0,
              total:    0
            });
          }
        });

        it('has valid display state when there are entities for criteria', function() {
          var controller = createController(getItemsWrapper);

          expect(controller.displayState).toBe(2); // HAVE_RESULTS

          function getItemsWrapper(options) {
            getItemsSpy(options);
            return $q.when({
              items:    entities.slice(0, pageSize),
              page:     options.page,
              pageSize: pageSize,
              offset:   0,
              total:    entities.length + 1
            });
          }
        });

        it('has valid display state when there are no entities', function() {
          var controller;

          counts = _.mapObject(counts, function (val) {
            return 0;
          });
          controller = createController(getItemsWrapperDefault);
          expect(controller.displayState).toBe(0); // NO_ENTITIES
        });

      });

    }

  });

});
