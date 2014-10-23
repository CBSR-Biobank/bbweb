// Jasmine test suite
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Service: centresService', function() {

    var centresService, httpBackend;

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function (_centresService_, $httpBackend) {
      centresService = _centresService_;
      httpBackend = $httpBackend;
    }));

    afterEach(function() {
      httpBackend.verifyNoOutstandingExpectation();
      httpBackend.verifyNoOutstandingRequest();
    });

    it('should return valid data', function() {
      httpBackend.whenGET('/centres').respond({
        status: 'success',
        data: [
          {
            id:           'dummy-id',
            name:         'CTR1',
            status:       'Disabled',
            version:      5,
            timeAdded:    '2014-10-20T09:58:43-0600'
          }
        ]
      });
      centresService.list().then(function(data) {
        //console.log(JSON.stringify(data));

        expect(data[0].id).toEqual('dummy-id');
        expect(data[0].name).toEqual('CTR1');
      });
      httpBackend.flush();
    });

  });

});
