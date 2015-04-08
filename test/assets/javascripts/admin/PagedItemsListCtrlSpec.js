/**
 * Jasmine test suite
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobank.testUtils',
  'biobankApp'
], function(angular, mocks, _, testUtils) {
  'use strict';

  describe('Controller: PagedItemsListCtrl', function() {
    var fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(fakeDomainEntities) {
      fakeEntities = fakeDomainEntities;
      testUtils.addCustomMatchers();
    }));

    describe('Centres', function () {
      var context = {};

      beforeEach(inject(function ($q, Centre, CentreStatus) {
        var disabledStudies, enabledStudies;

        disabledStudies = _.map(_.range(2), function() {
          return new fakeEntities.centre();
        });
        enabledStudies = _.map(_.range(3), function() {
          var centre = new fakeEntities.centre();
          centre.status = CentreStatus.ENABLED();
          return centre;
        });

        context.entities                     = disabledStudies.concat(enabledStudies);
        context.counts                       = createCounts(disabledStudies.length, enabledStudies.length);
        context.pageSize                     = disabledStudies.length;
        context.messageNoItems               = 'No items present';
        context.messageNoResults             = 'No items match the criteria';
        context.entityNavigateState          = 'home.admin.centres.centre.summary';
        context.entityNavigateStateParamName = 'centreId';

        context.possibleStatuses = _.map(['all'].concat(CentreStatus.values()), function (status) {
          return { id: status.toLowerCase(), name: status };
        });
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

        context.entities                     = disabledStudies.concat(enabledStudies.concat(retiredStudies));
        context.counts                       = createCounts(disabledStudies.length,
                                                            enabledStudies.length,
                                                            retiredStudies.length);
        context.pageSize                     = disabledStudies.length;
        context.messageNoItems               = 'No items present';
        context.messageNoResults             = 'No items match the criteria';
        context.entityNavigateState          = 'home.admin.studies.study.summary';
        context.entityNavigateStateParamName = 'studyId';

        context.possibleStatuses = _.map(['all'].concat(StudyStatus.values()), function (status) {
          return { id: status.toLowerCase(), name: status };
        });
      }));

      function createCounts(disabled, enabled, retired) {
        return {
          total:    disabled + enabled + retired,
          disabled: disabled,
          enabled:  enabled,
          retired:  retired
        };
      }

      sharedBehaviour(context);

    });

    function sharedBehaviour(context) {
      var $q,
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

        beforeEach(inject(function (_$q_) {
          $q                           = _$q_;
          entities                     = context.entities;
          counts                       = context.counts;
          pageSize                     = context.pageSize;
          possibleStatuses             = context.possibleStatuses;
          messageNoItems               = context.messageNoItems;
          messageNoResults             = context.messageNoResults;
          entityNavigateState          = context.entityNavigateState;
          entityNavigateStateParamName = context.entityNavigateStateParamName;

          createController = setupController(this.$injector);
        }));

        function setupController(injector) {
          var $rootScope  = injector.get('$rootScope'),
              $controller = injector.get('$controller');

          return create;

          //--

          function create(getItemsWrapper) {
            var scope = $rootScope.$new();

            scope.counts                  = counts;
            scope.pageSize                = pageSize;
            scope.possibleStatuses        = possibleStatuses;
            scope.messageNoItems          = messageNoItems;
            scope.messageNoResults        = messageNoResults;
            scope.getItems                = getItemsWrapper;
            scope.entityNavigateState     = entityNavigateState;
            scope.entityNavigateStateParamName = entityNavigateStateParamName;

            $controller('PagedItemsListCtrl as vm', {
              $scope: scope
            });
            scope.$digest();
            return scope;

            //--
          }
        }

        function getItemsWrapperDefault() {
          return function (options) {
            getItemsSpy(options);
            return $q.when({
              items:    entities.slice(0, pageSize),
              page:     options.page,
              pageSize: pageSize,
              offset:   0,
              total:    entities.length
            });
              };
        }

        it('has valid scope', function() {
          var scope = createController(getItemsWrapperDefault);

          expect(scope.vm.counts).toBe(counts);
          expect(scope.vm.possibleStatuses).toBe(possibleStatuses);
          expect(scope.vm.messageNoItems).toBe(messageNoItems);
          expect(scope.vm.messageNoResults).toBe(messageNoResults);
          expect(scope.vm.entityNavigateState).toBe(entityNavigateState);
          expect(scope.vm.entityNavigateStateParamName).toBe(entityNavigateStateParamName);
          expect(scope.vm.paginationNumPages).toBe(5);
          expect(scope.vm.sortFields).toContainAll(['Name', 'Status']);

          expect(scope.vm.pagerOptions.filter).toBeEmptyString();
          expect(scope.vm.pagerOptions.status).toBe(possibleStatuses[0]);
          expect(scope.vm.pagerOptions.pageSize).toBe(pageSize);
          expect(scope.vm.pagerOptions.sortField).toBe('name');
        });

        it('has a valid panel heading', function() {
          var scope = createController(getItemsWrapperDefault);
          _.each(possibleStatuses, function (status) {
            if (status.id !== 'all') {
              expect(scope.vm.panelHeading).toContain(status.name);
            }
          });
        });

        it('updates items when name filter is updated', function() {
          var scope = createController(getItemsWrapperDefault),
              nameFilterValue = 'test';

          scope.vm.pagerOptions.filter = nameFilterValue;
          scope.vm.nameFilterUpdated();
          scope.$digest();
          expect(getItemsSpy.calls.mostRecent().args[0]).toEqual({
            filter: nameFilterValue,
            status: possibleStatuses[0],
            page: 1,
            pageSize: pageSize,
            sortField: 'name'
          });
        });

        it('updates items when name status filter is updated', function() {
          var scope = createController(getItemsWrapperDefault),
              statusFilterValue = possibleStatuses[1];

          scope.vm.pagerOptions.status = statusFilterValue;
          scope.vm.statusFilterUpdated();
          scope.$digest();
          expect(getItemsSpy.calls.mostRecent().args[0]).toEqual({
            filter: '',
            status: statusFilterValue,
            page: 1,
            pageSize: pageSize,
            sortField: scope.vm.sortFields[0].toLowerCase()
          });
        });

        it('clears filters', function() {
          var scope = createController(getItemsWrapperDefault);

          scope.vm.pagerOptions.filter = 'test';
          scope.vm.pagerOptions.status = possibleStatuses[1];
          scope.vm.clearFilters();
          scope.$digest();
          expect(scope.vm.pagerOptions.filter).toBeNull();
          expect(scope.vm.pagerOptions.status).toBe(possibleStatuses[0]);
        });

        it('updates items when name sort field is updated', function() {
          var scope = createController(getItemsWrapperDefault),
              sortFieldValue = scope.vm.sortFields[1];

          scope.vm.sortFieldSelected(sortFieldValue);
          scope.$digest();
          expect(getItemsSpy.calls.mostRecent().args[0]).toEqual({
            filter: '',
            status: possibleStatuses[0],
            page: 1,
            pageSize: pageSize,
            sortField: sortFieldValue.toLowerCase()
          });
        });

        it('updates items when name page number is changed', function() {
          var scope = createController(getItemsWrapperDefault),
              page = 2;

          scope.vm.pagerOptions.page = page;
          scope.vm.pageChanged();
          scope.$digest();
          expect(getItemsSpy.calls.mostRecent().args[0]).toEqual({
            filter: '',
            status: possibleStatuses[0],
            page: page,
            pageSize: pageSize,
            sortField: scope.vm.sortFields[0].toLowerCase()
          });
        });

        it('has valid display state when there are no entities for criteria', function() {
          var scope = createController(getItemsWrapper);

          expect(scope.vm.displayState).toBe(1); // NO_RESULTS

          function getItemsWrapper() {
            return function (options) {
              getItemsSpy(options);
              return $q.when({
                items:    entities.slice(0, pageSize),
                page:     options.page,
                pageSize: pageSize,
                offset:   0,
                total:    0
              });
            };
          }
        });

        it('has valid display state when there are entities for criteria', function() {
          var scope = createController(getItemsWrapper);

          expect(scope.vm.displayState).toBe(2); // HAVE_RESULTS

          function getItemsWrapper() {
            return function (options) {
              getItemsSpy(options);
              return $q.when({
                items:    entities.slice(0, pageSize),
                page:     options.page,
                pageSize: pageSize,
                offset:   0,
                total:    entities.length + 1
              });
            };
          }
        });

        it('has valid display state when there are no entities', function() {
          var scope;

          counts = _.mapObject(counts, function (val) {
            return 0;
          });
          scope = createController(getItemsWrapperDefault);
          expect(scope.vm.displayState).toBe(0); // NO_ENTITIES
        });

      });

    }

  });

});
