//
//  FCCHttpClient.h
//  Classroom Checkout
//
//  Created by Mike Fikes on 1/1/15.
//  Copyright (c) 2015 FikesFarm. All rights reserved.
//

#import <Foundation/Foundation.h>

#import <JavaScriptCore/JavaScriptCore.h>

@class FCJHttpClient;

@protocol FCJHttpClient<JSExport>

+ (FCJHttpClient*)create;
- (void)requestMethod:(NSString*)method url:(NSString*)url parameters:(NSDictionary*)parameters callback:(JSValue *)callback;

@end

@interface FCJHttpClient : NSObject<FCJHttpClient>

@end
