// Jasmine test suite
define([
  'angular',
  'angularMocks',
  'underscore',
  'jquery',
  'biobankApp',
  'biobankTest'
], function(angular, mocks, _, $) {
  'use strict';

  describe('Service: centresService', function() {

    var httpBackend, centresService, Centre, fakeEntities;
    var serverCentre, serverCentreNoId;

    function uri(centreId) {
      var result = '/centres';
      if (arguments.length > 0) {
        result += '/' + centreId;
      }
      return result;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($httpBackend,
                                _centresService_,
                                _Centre_,
                                fakeDomainEntities,
                                extendedDomainEntities) {
      httpBackend      = $httpBackend;
      centresService   = _centresService_;
      Centre           = _Centre_;
      fakeEntities     = fakeDomainEntities;
      serverCentre     = fakeEntities.centre();
      serverCentreNoId = _.omit(serverCentre, 'id');
    }));

    afterEach(function() {
      httpBackend.verifyNoOutstandingExpectation();
      httpBackend.verifyNoOutstandingRequest();
    });

    it('should have the following functions', function () {
      expect(centresService.getCentreCounts).toBeFunction();
      expect(centresService.list).toBeFunction();
      expect(centresService.get).toBeFunction();
      expect(centresService.addOrUpdate).toBeFunction();
      expect(centresService.enable).toBeFunction();
      expect(centresService.disable).toBeFunction();
      expect(centresService.studies).toBeFunction();
      expect(centresService.addStudy).toBeFunction();
      expect(centresService.removeStudy).toBeFunction();
    });

    it('calling getCentreCounts has valid URL', function() {
      var response = {count: 1};
      httpBackend.whenGET(uri() + '/counts').respond({
        status: 'success',
        data: response
      });

      centresService.getCentreCounts().then(function(data) {
        expect(data).toEqual(response);
      });

      httpBackend.flush();
    });

    it('calling list with no parameters has no query string', function() {
      listSharedBehaviour(serverCentre);
    });

    it('calling list with filter parameter has valid query string', function() {
      listSharedBehaviour(serverCentre,  { filter: 'nameFilter' });
    });

    it('calling list with sort parameter has valid query string', function() {
      listSharedBehaviour(serverCentre, { sort: 'status' });
    });

    it('calling list with filter and status parameters has valid query string', function() {
      listSharedBehaviour(serverCentre, { filter: 'nameFilter', order: 'desc' });
    });

    it('calling list with page and pageSize parameters has valid query string', function() {
      listSharedBehaviour(serverCentre, { page: 1, pageSize: 5 });
    });

    it('calling list with sortField and order parameters has valid query string', function() {
      listSharedBehaviour(serverCentre, { sort: 'name', order: 'asc' });
    });

    it('get should return valid object', function() {
      httpBackend.whenGET(uri(serverCentre.id)).respond({
        status: 'success',
        data: serverCentre
      });

      centresService.get(serverCentre.id).then(function(centre) {
        expect(centre).toEqual(serverCentre);
      });

      httpBackend.flush();
    });

    it('should allow adding a centre', function() {
      var centre = new Centre(_.omit(fakeEntities.centre(), 'id'));
      var expectedResult = { status: 'success', data: 'success' };
      httpBackend.expectPOST(uri(), {name: centre.name, description: centre.description})
        .respond(201, expectedResult);

      centresService.addOrUpdate(centre).then(function(reply) {
        expect(reply).toEqual('success');
      });
      httpBackend.flush();
    });

    it('should allow updating a centre', function() {
      var centre = new Centre(fakeEntities.centre(), 'id');
      var expectedResult = { status: 'success', data: 'success' };
      httpBackend.expectPUT(
        uri(centre.id),
        {
          id:              centre.id,
          expectedVersion: centre.version,
          name:            centre.name,
          description:     centre.description
        })
        .respond(201, expectedResult);

      centresService.addOrUpdate(centre).then(function(reply) {
        expect(reply).toEqual('success');
      });
      httpBackend.flush();
    });

    it('should enable a centre', function() {
      statusChangeShared('enable', centresService.enable);
    });

    it('should disable a centre', function() {
      statusChangeShared('disable', centresService.disable);
    });

    it('should get the studies linked to a centre', function() {
      var study = fakeEntities.study();
      var centre = new Centre(fakeEntities.centre());
      httpBackend.whenGET(uri(centre.id) + '/studies').respond({
        status: 'success',
        data: [study.id]
      });

      centresService.studies(centre).then(function(reply) {
        expect(reply.length).toEqual(1);
        expect(reply[0]).toEqual(study.id);
      });

      httpBackend.flush();
    });

    it('should add a study to a centre', function() {
      var study = fakeEntities.study();
      var centre = fakeEntities.centre();
      var expectedCmd = {centreId: centre.id, studyId: study.id};
      var expectedResult = {status: 'success', data: {centreId: centre.id, studyId: study.id}};
      httpBackend.expectPOST(uri(centre.id) + '/studies/' + study.id, expectedCmd)
        .respond(201, expectedResult);
      centresService.addStudy(centre, study.id).then(function(reply) {
        expect(reply).toEqual(expectedResult.data);
      });
      httpBackend.flush();
    });

    it('should remove a study from a centre', function() {
      var numStudies;
      var study = fakeEntities.study();
      var centre = new Centre(fakeEntities.centre());
      var expectedResult = {status: 'success', data: {centreId: centre.id, studyId: study.id}};

      numStudies = centre.studyIds.length;

      httpBackend.expectDELETE(uri(centre.id) + '/studies/' + study.id)
        .respond(201, expectedResult);
      centresService.removeStudy(centre, study.id).then(function(reply) {
        expect(reply).toEqual(expectedResult);
      });
      httpBackend.flush();
    });

    function listSharedBehaviour(serverCentre, options) {
      var url;
      var params = '';
      options = options || {};

      if (options) {
        params += $.param(options);
      }
      if (params !== '') {
        params = '?' + params;
      }
      url = uri() + params;
      httpBackend.whenGET(url).respond({
        status: 'success',
        data: [serverCentre]
      });

      centresService.list(options).then(function(centres) {
        expect(centres.length).toEqual(1);
        _.each(centres, function(centre) {
          expect(centre).toEqual(serverCentre);
        });
      });

      httpBackend.flush();
    }

    function statusChangeShared(status, serviceFn) {
      var centre = fakeEntities.centre();
      var expectedCmd = { id: centre.id, expectedVersion: centre.version};
      var expectedResult = {status: 'success', data: 'success'};
      httpBackend.expectPOST(uri(centre.id) + '/' + status, expectedCmd).respond(201, expectedResult);
      serviceFn(centre).then(function(reply) {
        expect(reply).toBe('success');
      });
      httpBackend.flush();
    }

  });

});
